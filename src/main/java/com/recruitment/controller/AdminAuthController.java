package com.recruitment.controller;

import com.recruitment.service.AdminAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.recruitment.dto.request.LoginRequest;
import com.recruitment.dto.request.RefreshRequest;
import com.recruitment.dto.response.AdminUserResponse;
import com.recruitment.dto.response.ApiResponse;
import com.recruitment.dto.response.LoginResponse;
import com.recruitment.exception.InvalidTokenException;

import java.util.Arrays;

@RestController
@RequestMapping("/api/v1/admin/auth")
public class AdminAuthController {

	private static final String REFRESH_TOKEN_COOKIE_NAME = "admin_refresh_token";
	private static final String COOKIE_PATH = "/api/v1/admin/auth";
	private static final int COOKIE_MAX_AGE = 259200;

	private final AdminAuthService adminAuthService;

	public AdminAuthController(AdminAuthService adminAuthService) {
		this.adminAuthService = adminAuthService;
	}

	@PostMapping("/login")
	public ResponseEntity<ApiResponse<LoginResponse>> login(
			@Valid @RequestBody LoginRequest request,
			HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) {

		String deviceInfo = httpRequest.getHeader("User-Agent");
		String ipAddress = extractClientIp(httpRequest);

		LoginResponse loginResponse = adminAuthService.login(request, deviceInfo, ipAddress);
		setRefreshTokenCookie(httpResponse, loginResponse.refreshToken());

		return ResponseEntity.ok(ApiResponse.success("Đăng nhập admin thành công", loginResponse));
	}

	@PostMapping("/refresh")
	public ResponseEntity<ApiResponse<LoginResponse>> refresh(
			@RequestBody(required = false) RefreshRequest body,
			HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) {

		String rawRefreshToken = extractRefreshToken(body, httpRequest);
		if (rawRefreshToken == null) {
			throw new InvalidTokenException("Refresh token không được cung cấp");
		}

		LoginResponse loginResponse = adminAuthService.refresh(rawRefreshToken);
		setRefreshTokenCookie(httpResponse, loginResponse.refreshToken());

		return ResponseEntity.ok(ApiResponse.success("Token đã được làm mới", loginResponse));
	}

	@PostMapping("/logout")
	public ResponseEntity<ApiResponse<Void>> logout(
			HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) {

		String rawRefreshToken = extractRefreshTokenFromCookie(httpRequest);
		if (rawRefreshToken != null) {
			adminAuthService.logout(rawRefreshToken);
		}
		clearRefreshTokenCookie(httpResponse);

		return ResponseEntity.ok(ApiResponse.success("Đăng xuất thành công", null));
	}

	@GetMapping("/me")
	public ResponseEntity<ApiResponse<AdminUserResponse>> getMe(@AuthenticationPrincipal Jwt jwt) {
		AdminUserResponse response = adminAuthService.getMe(jwt.getSubject());
		return ResponseEntity.ok(ApiResponse.success(response));
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
				.filter(cookie -> cookie != null && REFRESH_TOKEN_COOKIE_NAME.equals(cookie.getName()))
				.map(cookie -> cookie.getValue())
				.findFirst()
				.orElse(null);
	}

	private void setRefreshTokenCookie(HttpServletResponse response, String rawToken) {
		response.setHeader("Set-Cookie",
				REFRESH_TOKEN_COOKIE_NAME + "=" + rawToken
						+ "; HttpOnly; Secure; SameSite=Lax; Path=" + COOKIE_PATH
						+ "; Max-Age=" + COOKIE_MAX_AGE);
	}

	private void clearRefreshTokenCookie(HttpServletResponse response) {
		response.setHeader("Set-Cookie", REFRESH_TOKEN_COOKIE_NAME + "=; Max-Age=0; Path=" + COOKIE_PATH);
	}

	private String extractClientIp(HttpServletRequest request) {
		String xForwardedFor = request.getHeader("X-Forwarded-For");
		if (xForwardedFor != null && !xForwardedFor.isBlank()) {
			return xForwardedFor.split(",")[0].trim();
		}
		return request.getRemoteAddr();
	}
}
