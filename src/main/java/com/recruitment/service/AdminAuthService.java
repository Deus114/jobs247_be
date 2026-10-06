package com.recruitment.service;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.recruitment.dto.request.admin.auth.LoginRequest;
import com.recruitment.dto.request.admin.auth.RefreshRequest;
import com.recruitment.dto.response.admin.auth.LoginResponse;
import com.recruitment.dto.response.common.ApiResponse;
import com.recruitment.entity.AdminUser;
import com.recruitment.entity.RefreshToken;
import com.recruitment.exception.InvalidTokenException;
import com.recruitment.mapper.AdminUserMapper;
import com.recruitment.security.JwtTokenProvider;
import com.recruitment.util.constant.Constants;
import com.recruitment.util.helper.RefreshCookieHelper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class AdminAuthService {

	private static final Logger log = LoggerFactory.getLogger(AdminAuthService.class);

	private final AuthenticationManager authenticationManager;
	private final AdminUserService adminUserService;
	private final AdminUserMapper adminUserMapper;
	private final JwtTokenProvider jwtTokenProvider;
	private final RefreshTokenService refreshTokenService;
	private final RefreshCookieHelper refreshCookieHelper;
	private final MessageService messageService;

	public AdminAuthService(
			AuthenticationManager authenticationManager,
			AdminUserService adminUserService,
			AdminUserMapper adminUserMapper,
			JwtTokenProvider jwtTokenProvider,
			RefreshTokenService refreshTokenService,
			RefreshCookieHelper refreshCookieHelper,
			MessageService messageService) {
		this.authenticationManager = authenticationManager;
		this.adminUserService = adminUserService;
		this.adminUserMapper = adminUserMapper;
		this.jwtTokenProvider = jwtTokenProvider;
		this.refreshTokenService = refreshTokenService;
		this.refreshCookieHelper = refreshCookieHelper;
		this.messageService = messageService;
	}

	@Transactional
	public ResponseEntity<ApiResponse<LoginResponse>> login(
			LoginRequest request,
			HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) {

		String deviceInfo = httpRequest.getHeader("User-Agent");
		String ipAddress = extractClientIp(httpRequest);

		authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(request.userName(), request.password()));

		AdminUser adminUser = adminUserService.getByUsernameWithRolePermissions(request.userName());

		String accessToken = jwtTokenProvider.generateAccessToken(adminUser);
		String refreshToken = jwtTokenProvider.generateRefreshToken(adminUser);
		refreshTokenService.save(refreshToken, adminUser, deviceInfo, ipAddress);
		refreshCookieHelper.set(
				httpResponse,
				Constants.ADMIN_REFRESH_TOKEN_COOKIE_NAME,
				Constants.ADMIN_AUTH_BASE_PATH,
				refreshToken);

		log.info("Admin đăng nhập thành công: {}", adminUser.getUsername());

		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("auth.login.success"),
				new LoginResponse(accessToken, adminUserMapper.toResponse(adminUser))));
	}

	@Transactional
	public ResponseEntity<ApiResponse<LoginResponse>> refresh(
			RefreshRequest body,
			HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) {

		String rawRefreshToken = extractRefreshToken(body, httpRequest);
		if (rawRefreshToken == null) {
			throw new InvalidTokenException("error.refresh-token.missing");
		}

		RefreshToken storedToken = refreshTokenService.validateForRefresh(rawRefreshToken);
		refreshTokenService.revoke(storedToken);

		AdminUser adminUser = storedToken.getAdminUser();
		String accessToken = jwtTokenProvider.generateAccessToken(adminUser);
		String refreshToken = jwtTokenProvider.generateRefreshToken(adminUser);
		refreshTokenService.save(refreshToken, adminUser, storedToken.getDeviceInfo(), storedToken.getIpAddress());
		refreshCookieHelper.set(
				httpResponse,
				Constants.ADMIN_REFRESH_TOKEN_COOKIE_NAME,
				Constants.ADMIN_AUTH_BASE_PATH,
				refreshToken);

		log.info("Refresh token được làm mới cho admin: {}", adminUser.getUsername());

		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("auth.refresh.success"),
				new LoginResponse(accessToken, adminUserMapper.toResponse(adminUser))));
	}

	@Transactional
	public ResponseEntity<ApiResponse<Void>> logout(
			HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) {

		String rawRefreshToken = extractRefreshTokenFromCookie(httpRequest);
		if (rawRefreshToken != null) {
			refreshTokenService.revokeSessionByRawToken(rawRefreshToken);
		}
		refreshCookieHelper.clear(
				httpResponse,
				Constants.ADMIN_REFRESH_TOKEN_COOKIE_NAME,
				Constants.ADMIN_AUTH_BASE_PATH);
		log.info("Admin đăng xuất thiết bị hiện tại");
		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("auth.logout.success"), null));
	}

	private String extractRefreshToken(RefreshRequest body, HttpServletRequest request) {
		String fromCookie = extractRefreshTokenFromCookie(request);
		if (fromCookie != null) {
			return fromCookie;
		}
		if (body != null && body.refreshToken() != null && !body.refreshToken().isBlank()) {
			return body.refreshToken();
		}
		return null;
	}

	private String extractRefreshTokenFromCookie(HttpServletRequest request) {
		if (request.getCookies() == null) {
			return null;
		}
		return Arrays.stream(request.getCookies())
				.filter(cookie -> cookie != null
						&& Constants.ADMIN_REFRESH_TOKEN_COOKIE_NAME.equals(cookie.getName()))
				.map(cookie -> cookie.getValue())
				.findFirst()
				.orElse(null);
	}

	private String extractClientIp(HttpServletRequest request) {
		String xForwardedFor = request.getHeader("X-Forwarded-For");
		if (xForwardedFor != null && !xForwardedFor.isBlank()) {
			return xForwardedFor.split(",")[0].trim();
		}
		return request.getRemoteAddr();
	}
}
