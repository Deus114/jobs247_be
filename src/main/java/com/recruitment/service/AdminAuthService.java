package com.recruitment.service;

import java.util.Arrays;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.recruitment.dto.request.auth.LoginRequest;
import com.recruitment.dto.request.auth.RefreshRequest;
import com.recruitment.dto.response.auth.LoginResponse;
import com.recruitment.dto.response.common.ApiResponse;
import com.recruitment.entity.AdminUser;
import com.recruitment.entity.RefreshToken;
import com.recruitment.exception.InvalidTokenException;
import com.recruitment.mapper.AdminUserMapper;
import com.recruitment.security.JwtTokenProvider;
import com.recruitment.util.constant.Constants;

@Service
public class AdminAuthService {

	private static final Logger log = LoggerFactory.getLogger(AdminAuthService.class);

	private final AuthenticationManager authenticationManager;
	private final AdminUserService adminUserService;
	private final AdminUserMapper adminUserMapper;
	private final JwtTokenProvider jwtTokenProvider;
	private final RefreshTokenService refreshTokenService;
	private final MessageService messageService;

	public AdminAuthService(
			AuthenticationManager authenticationManager,
			AdminUserService adminUserService,
			AdminUserMapper adminUserMapper,
			JwtTokenProvider jwtTokenProvider,
			RefreshTokenService refreshTokenService,
			MessageService messageService) {
		this.authenticationManager = authenticationManager;
		this.adminUserService = adminUserService;
		this.adminUserMapper = adminUserMapper;
		this.jwtTokenProvider = jwtTokenProvider;
		this.refreshTokenService = refreshTokenService;
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

		log.info("Admin đăng nhập thành công: {}", adminUser.getUsername());

		LoginResponse loginResponse = toLoginResponse(accessToken, refreshToken, adminUser);
		setRefreshTokenCookie(httpResponse, loginResponse.refreshToken());

		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("auth.login.success"), loginResponse));
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

		log.info("Refresh token được làm mới cho admin: {}", adminUser.getUsername());

		LoginResponse loginResponse = toLoginResponse(accessToken, refreshToken, adminUser);
		setRefreshTokenCookie(httpResponse, loginResponse.refreshToken());

		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("auth.refresh.success"), loginResponse));
	}

	@Transactional
	public ResponseEntity<ApiResponse<Void>> logout(
			String username,
			HttpServletResponse httpResponse) {

		refreshTokenService.revokeAllActiveByUsername(username);
		clearRefreshTokenCookie(httpResponse);

		log.info("Admin đăng xuất thành công: {}", username);

		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("auth.logout.success"), null));
	}

	private LoginResponse toLoginResponse(String accessToken, String refreshToken, AdminUser adminUser) {
		return new LoginResponse(accessToken, refreshToken, adminUserMapper.toResponse(adminUser));
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

	private void setRefreshTokenCookie(HttpServletResponse response, String rawToken) {
		response.setHeader("Set-Cookie",
				Constants.ADMIN_REFRESH_TOKEN_COOKIE_NAME + "=" + rawToken
						+ "; HttpOnly; Secure; SameSite=Lax; Path=" + Constants.ADMIN_REFRESH_TOKEN_COOKIE_PATH
						+ "; Max-Age=" + Constants.ADMIN_REFRESH_TOKEN_COOKIE_MAX_AGE);
	}

	private void clearRefreshTokenCookie(HttpServletResponse response) {
		response.setHeader("Set-Cookie",
				Constants.ADMIN_REFRESH_TOKEN_COOKIE_NAME + "=; Max-Age=0; Path="
						+ Constants.ADMIN_REFRESH_TOKEN_COOKIE_PATH);
	}

	private String extractClientIp(HttpServletRequest request) {
		String xForwardedFor = request.getHeader("X-Forwarded-For");
		if (xForwardedFor != null && !xForwardedFor.isBlank()) {
			return xForwardedFor.split(",")[0].trim();
		}
		return request.getRemoteAddr();
	}
}
