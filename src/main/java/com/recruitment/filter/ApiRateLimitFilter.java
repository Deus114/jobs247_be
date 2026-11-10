package com.recruitment.filter;

import java.io.IOException;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.recruitment.dto.response.common.ApiResponse;
import com.recruitment.service.ApiRateLimitService;
import com.recruitment.service.MessageService;
import com.recruitment.service.RateLimitDecision;
import com.recruitment.util.constant.Constants;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.json.JsonMapper;

@Component
public class ApiRateLimitFilter extends OncePerRequestFilter {

	private static final Logger log = LoggerFactory.getLogger(ApiRateLimitFilter.class);

	private static final Set<String> AUTH_PATHS = Set.of(
			Constants.AUTH_LOGIN_PATH,
			Constants.AUTH_REFRESH_PATH,
			Constants.AUTH_REGISTER_PATH,
			Constants.AUTH_OTP_SEND_PATH,
			Constants.AUTH_OTP_VERIFY_PATH,
			Constants.ADMIN_AUTH_LOGIN_PATH,
			Constants.ADMIN_AUTH_REFRESH_PATH);

	private static final Set<String> WRITE_EXACT_PATHS = Set.of(
			Constants.EMPLOYER_COMPANIES_BASE_PATH,
			Constants.EMPLOYER_JOBS_BASE_PATH,
			Constants.USER_DEVICE_TOKENS_BASE_PATH,
			Constants.ADMIN_DEVICE_TOKENS_BASE_PATH);

	private static final String JOB_SEEKER_APPLICATION_PATTERN = Constants.JOB_SEEKER_API_BASE_PATH
			+ "/jobs/*/applications";

	private final ApiRateLimitService apiRateLimitService;
	private final MessageService messageService;
	private final JsonMapper jsonMapper;
	private final AntPathMatcher pathMatcher = new AntPathMatcher();

	public ApiRateLimitFilter(
			ApiRateLimitService apiRateLimitService,
			MessageService messageService,
			JsonMapper jsonMapper) {
		this.apiRateLimitService = apiRateLimitService;
		this.messageService = messageService;
		this.jsonMapper = jsonMapper;
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		if (!apiRateLimitService.isEnabled()) {
			return true;
		}
		if (HttpMethod.OPTIONS.matches(request.getMethod())) {
			return true;
		}
		return resolveGroup(request) == null;
	}

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {
		RateLimitGroup group = resolveGroup(request);
		if (group == null) {
			filterChain.doFilter(request, response);
			return;
		}

		String identity = identityFor(group, request);
		RateLimitDecision decision = switch (group) {
			case AUTH -> apiRateLimitService.consumeAuth(identity);
			case WRITE -> apiRateLimitService.consumeWrite(identity);
			case READ -> apiRateLimitService.consumeRead(identity);
		};

		switch (decision.outcome()) {
			case LIMITED -> {
				log.warn("Rate limit exceeded group={} identity={} path={}",
						group, identity, request.getRequestURI());
				writeRejection(
						response,
						HttpStatus.TOO_MANY_REQUESTS,
						"error.rate-limit.exceeded",
						decision.retryAfterSeconds());
			}
			case UNAVAILABLE -> {
				log.warn("Rate limit unavailable group={} identity={} path={}",
						group, identity, request.getRequestURI());
				writeRejection(
						response,
						HttpStatus.SERVICE_UNAVAILABLE,
						"error.rate-limit.unavailable",
						decision.retryAfterSeconds());
			}
			case ALLOWED -> filterChain.doFilter(request, response);
		}
	}

	private RateLimitGroup resolveGroup(HttpServletRequest request) {
		String method = request.getMethod();
		String path = request.getRequestURI();

		if (HttpMethod.POST.matches(method) && AUTH_PATHS.contains(path)) {
			return RateLimitGroup.AUTH;
		}
		if (HttpMethod.POST.matches(method) && isWritePath(path)) {
			return RateLimitGroup.WRITE;
		}
		if (HttpMethod.GET.matches(method) && isReadPath(path)) {
			return RateLimitGroup.READ;
		}
		return null;
	}

	private boolean isWritePath(String path) {
		if (WRITE_EXACT_PATHS.contains(path)) {
			return true;
		}
		return pathMatcher.match(JOB_SEEKER_APPLICATION_PATTERN, path);
	}

	private boolean isReadPath(String path) {
		return path.equals(Constants.PUBLIC_JOBS_BASE_PATH)
				|| pathMatcher.match(Constants.PUBLIC_JOBS_BASE_PATH + "/**", path)
				|| path.equals(Constants.PUBLIC_COMPANIES_BASE_PATH)
				|| pathMatcher.match(Constants.PUBLIC_COMPANIES_BASE_PATH + "/**", path);
	}

	private String identityFor(RateLimitGroup group, HttpServletRequest request) {
		String clientIp = clientIp(request);
		if (group == RateLimitGroup.AUTH) {
			return clientIp;
		}
		Long userId = currentUserId();
		if (userId != null) {
			return "user:" + userId;
		}
		return "ip:" + clientIp;
	}

	private static Long currentUserId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (!(authentication instanceof JwtAuthenticationToken jwtToken)) {
			return null;
		}
		Object claim = jwtToken.getToken().getClaim("userId");
		if (claim instanceof Number number) {
			return number.longValue();
		}
		if (claim instanceof String text && StringUtils.hasText(text)) {
			try {
				return Long.parseLong(text.trim());
			} catch (NumberFormatException ignored) {
				return null;
			}
		}
		return null;
	}

	private void writeRejection(
			HttpServletResponse response,
			HttpStatus status,
			String messageKey,
			long retryAfterSeconds) throws IOException {
		response.setStatus(status.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");
		response.setHeader(HttpHeaders.RETRY_AFTER, Long.toString(retryAfterSeconds));
		ApiResponse<Void> body = ApiResponse.of(status.value(), messageService.get(messageKey), null);
		response.getWriter().write(jsonMapper.writeValueAsString(body));
	}

	private static String clientIp(HttpServletRequest request) {
		String remoteAddr = request.getRemoteAddr();
		if (!StringUtils.hasText(remoteAddr)) {
			return "unknown";
		}
		return remoteAddr;
	}

	private enum RateLimitGroup {
		AUTH,
		WRITE,
		READ
	}
}
