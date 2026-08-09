package com.recruitment.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.recruitment.dto.request.LoginRequest;
import com.recruitment.dto.response.AdminUserResponse;
import com.recruitment.dto.response.LoginResponse;
import com.recruitment.entity.AdminUser;
import com.recruitment.entity.RefreshToken;
import com.recruitment.exception.InvalidTokenException;
import com.recruitment.exception.ResourceNotFoundException;
import com.recruitment.mapper.AdminUserMapper;
import com.recruitment.repository.AdminUserRepository;
import com.recruitment.repository.RefreshTokenRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;

@Service
public class AdminAuthServiceImpl implements AdminAuthService {

	private static final Logger log = LoggerFactory.getLogger(AdminAuthServiceImpl.class);

	private final AuthenticationManager authenticationManager;
	private final JwtEncoder jwtEncoder;
	private final AdminUserRepository adminUserRepository;
	private final RefreshTokenRepository refreshTokenRepository;
	private final AdminUserMapper adminUserMapper;

	@Value("${jwt.access-token-expiration}")
	private long accessTokenExpiration;

	@Value("${jwt.refresh-expiration}")
	private long refreshTokenExpiration;

	public AdminAuthServiceImpl(AuthenticationManager authenticationManager,
			JwtEncoder jwtEncoder,
			AdminUserRepository adminUserRepository,
			RefreshTokenRepository refreshTokenRepository,
			AdminUserMapper adminUserMapper) {
		this.authenticationManager = authenticationManager;
		this.jwtEncoder = jwtEncoder;
		this.adminUserRepository = adminUserRepository;
		this.refreshTokenRepository = refreshTokenRepository;
		this.adminUserMapper = adminUserMapper;
	}

	@Override
	@Transactional
	public LoginResponse login(LoginRequest request, String deviceInfo, String ipAddress) {
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(request.username(), request.password()));

		AdminUser adminUser = adminUserRepository.findByUsername(request.username())
				.orElseThrow(() -> new ResourceNotFoundException("Admin", "username", request.username()));

		String rawAccessToken = generateAccessToken(authentication, adminUser);
		String rawRefreshToken = generateRefreshToken(adminUser.getId(), adminUser.getUsername());

		saveRefreshToken(rawRefreshToken, adminUser, deviceInfo, ipAddress);

		log.info("Admin đăng nhập thành công: {}", adminUser.getUsername());
		return new LoginResponse(rawAccessToken, rawRefreshToken);
	}

	@Override
	@Transactional
	public LoginResponse refresh(String rawRefreshToken) {
		String tokenHash = hashToken(rawRefreshToken);

		RefreshToken storedToken = refreshTokenRepository.findByToken(tokenHash)
				.orElseThrow(() -> new InvalidTokenException("Refresh token không hợp lệ"));

		if (storedToken.isRevoked()) {
			throw new InvalidTokenException("Refresh token đã bị thu hồi");
		}

		if (storedToken.getExpiresAt().isBefore(Instant.now())) {
			throw new InvalidTokenException("Refresh token đã hết hạn");
		}

		storedToken.setRevoked(true);
		refreshTokenRepository.save(storedToken);

		AdminUser adminUser = storedToken.getAdminUser();
		String newAccessToken = generateAccessTokenFromUser(adminUser);
		String newRefreshToken = generateRefreshToken(adminUser.getId(), adminUser.getUsername());

		saveRefreshToken(newRefreshToken, adminUser, storedToken.getDeviceInfo(), storedToken.getIpAddress());

		log.info("Refresh token được làm mới cho admin: {}", adminUser.getUsername());
		return new LoginResponse(newAccessToken, newRefreshToken);
	}

	@Override
	@Transactional
	public void logout(String rawRefreshToken) {
		String tokenHash = hashToken(rawRefreshToken);

		RefreshToken storedToken = refreshTokenRepository.findByToken(tokenHash)
				.orElseThrow(() -> new InvalidTokenException("Refresh token không hợp lệ"));

		storedToken.setRevoked(true);
		refreshTokenRepository.save(storedToken);
		log.info("Admin đăng xuất thành công: {}", storedToken.getAdminUser().getUsername());
	}

	@Override
	@Transactional(readOnly = true)
	public AdminUserResponse getMe(String username) {
		AdminUser adminUser = adminUserRepository.findByUsername(username)
				.orElseThrow(() -> new ResourceNotFoundException("Admin", "username", username));
		return adminUserMapper.toResponse(adminUser);
	}

	private String generateAccessToken(Authentication authentication, AdminUser adminUser) {
		Instant now = Instant.now();
		List<String> roles = authentication.getAuthorities().stream()
				.map(authority -> authority == null ? "" : authority.getAuthority())
				.filter(role -> !role.isEmpty())
				.toList();

		JwtClaimsSet claims = JwtClaimsSet.builder()
				.subject(authentication.getName())
				.issuedAt(now)
				.expiresAt(now.plusMillis(accessTokenExpiration))
				.claim("userId", adminUser.getId())
				.claim("roles", roles)
				.claim("fullAccess", adminUser.getRole().isFullAccess())
				.build();

		JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
		return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
	}

	private String generateAccessTokenFromUser(AdminUser adminUser) {
		Instant now = Instant.now();
		String roleAuthority = "ROLE_" + adminUser.getRole().getName();

		JwtClaimsSet claims = JwtClaimsSet.builder()
				.subject(adminUser.getUsername())
				.issuedAt(now)
				.expiresAt(now.plusMillis(accessTokenExpiration))
				.claim("userId", adminUser.getId())
				.claim("roles", List.of(roleAuthority))
				.claim("fullAccess", adminUser.getRole().isFullAccess())
				.build();

		JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
		return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
	}

	private String generateRefreshToken(Long userId, String username) {
		Instant now = Instant.now();
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.id(java.util.UUID.randomUUID().toString())
				.subject(username)
				.issuedAt(now)
				.expiresAt(now.plusMillis(refreshTokenExpiration))
				.claim("userId", userId)
				.claim("type", "refresh")
				.build();

		JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
		return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
	}

	private void saveRefreshToken(String rawToken, AdminUser adminUser, String deviceInfo, String ipAddress) {
		RefreshToken refreshToken = new RefreshToken();
		refreshToken.setToken(hashToken(rawToken));
		refreshToken.setAdminUser(adminUser);
		refreshToken.setExpiresAt(Instant.now().plusMillis(refreshTokenExpiration));
		refreshToken.setDeviceInfo(deviceInfo);
		refreshToken.setIpAddress(ipAddress);
		refreshToken.setRevoked(false);
		refreshTokenRepository.save(refreshToken);
	}

	private String hashToken(String rawToken) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hashBytes = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(hashBytes);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("SHA-256 không khả dụng", e);
		}
	}
}
