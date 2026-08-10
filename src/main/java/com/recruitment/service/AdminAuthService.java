package com.recruitment.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.recruitment.dto.request.LoginRequest;
import com.recruitment.dto.response.LoginResponse;
import com.recruitment.entity.AdminUser;
import com.recruitment.entity.RefreshToken;
import com.recruitment.mapper.AdminUserMapper;
import com.recruitment.security.JwtTokenProvider;

@Service
public class AdminAuthService {

	private static final Logger log = LoggerFactory.getLogger(AdminAuthService.class);

	private final AuthenticationManager authenticationManager;
	private final AdminUserService adminUserService;
	private final AdminUserMapper adminUserMapper;
	private final JwtTokenProvider jwtTokenProvider;
	private final RefreshTokenService refreshTokenService;

	public AdminAuthService(
			AuthenticationManager authenticationManager,
			AdminUserService adminUserService,
			AdminUserMapper adminUserMapper,
			JwtTokenProvider jwtTokenProvider,
			RefreshTokenService refreshTokenService) {
		this.authenticationManager = authenticationManager;
		this.adminUserService = adminUserService;
		this.adminUserMapper = adminUserMapper;
		this.jwtTokenProvider = jwtTokenProvider;
		this.refreshTokenService = refreshTokenService;
	}

	@Transactional
	public LoginResponse login(LoginRequest request, String deviceInfo, String ipAddress) {
		authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(request.username(), request.password()));

		AdminUser adminUser = adminUserService.getByUsername(request.username());

		String accessToken = jwtTokenProvider.generateAccessToken(adminUser);
		String refreshToken = jwtTokenProvider.generateRefreshToken(adminUser);
		refreshTokenService.save(refreshToken, adminUser, deviceInfo, ipAddress);

		log.info("Admin đăng nhập thành công: {}", adminUser.getUsername());
		return toLoginResponse(accessToken, refreshToken, adminUser);
	}

	@Transactional
	public LoginResponse refresh(String rawRefreshToken) {
		RefreshToken storedToken = refreshTokenService.validateForRefresh(rawRefreshToken);
		refreshTokenService.revoke(storedToken);

		AdminUser adminUser = storedToken.getAdminUser();
		String accessToken = jwtTokenProvider.generateAccessToken(adminUser);
		String refreshToken = jwtTokenProvider.generateRefreshToken(adminUser);
		refreshTokenService.save(refreshToken, adminUser, storedToken.getDeviceInfo(), storedToken.getIpAddress());

		log.info("Refresh token được làm mới cho admin: {}", adminUser.getUsername());
		return toLoginResponse(accessToken, refreshToken, adminUser);
	}

	@Transactional
	public void logout(String rawRefreshToken) {
		refreshTokenService.revokeByRawToken(rawRefreshToken);
		log.info("Admin đăng xuất thành công");
	}

	private LoginResponse toLoginResponse(String accessToken, String refreshToken, AdminUser adminUser) {
		return new LoginResponse(accessToken, refreshToken, adminUserMapper.toResponse(adminUser));
	}
}
