package com.recruitment.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.recruitment.entity.AdminUser;
import com.recruitment.entity.RefreshToken;
import com.recruitment.exception.InvalidTokenException;
import com.recruitment.repository.RefreshTokenRepository;

@Service
public class RefreshTokenService {

	private final RefreshTokenRepository refreshTokenRepository;

	@Value("${jwt.refresh-expiration}")
	private long refreshTokenExpiration;

	public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
		this.refreshTokenRepository = refreshTokenRepository;
	}

	@Transactional
	public void save(String rawToken, AdminUser adminUser, String deviceInfo, String ipAddress) {
		RefreshToken refreshToken = new RefreshToken();
		refreshToken.setToken(hashToken(rawToken));
		refreshToken.setAdminUser(adminUser);
		refreshToken.setExpiresAt(Instant.now().plusMillis(refreshTokenExpiration));
		refreshToken.setDeviceInfo(deviceInfo);
		refreshToken.setIpAddress(ipAddress);
		refreshToken.setRevoked(false);
		refreshTokenRepository.save(refreshToken);
	}

	public RefreshToken validateForRefresh(String rawRefreshToken) {
		RefreshToken storedToken = refreshTokenRepository.findByToken(hashToken(rawRefreshToken))
				.orElseThrow(() -> new InvalidTokenException("error.refresh-token.invalid"));

		if (storedToken.isRevoked()) {
			throw new InvalidTokenException("error.refresh-token.revoked");
		}

		if (storedToken.getExpiresAt().isBefore(Instant.now())) {
			throw new InvalidTokenException("error.refresh-token.expired");
		}

		return storedToken;
	}

	@Transactional
	public void revoke(RefreshToken refreshToken) {
		refreshToken.setRevoked(true);
		refreshTokenRepository.save(refreshToken);
	}

	@Transactional
	public void revokeByRawToken(String rawRefreshToken) {
		RefreshToken storedToken = refreshTokenRepository.findByToken(hashToken(rawRefreshToken))
				.orElseThrow(() -> new InvalidTokenException("error.refresh-token.invalid"));
		revoke(storedToken);
	}

	@Transactional
	public void revokeAllActiveByUsername(String username) {
		refreshTokenRepository.revokeAllActiveByUsername(username);
	}

	@Transactional
	public void revokeSessionByRawToken(String rawRefreshToken) {
		refreshTokenRepository.findByToken(hashToken(rawRefreshToken)).ifPresent(stored -> {
			if (!stored.isRevoked()) {
				revoke(stored);
			}
		});
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
