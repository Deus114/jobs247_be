package com.recruitment.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.recruitment.entity.User;
import com.recruitment.entity.UserRefreshToken;
import com.recruitment.exception.InvalidTokenException;
import com.recruitment.repository.UserRefreshTokenRepository;

@Service
public class UserRefreshTokenService {

	private final UserRefreshTokenRepository userRefreshTokenRepository;

	@Value("${jwt.refresh-expiration}")
	private long refreshTokenExpiration;

	public UserRefreshTokenService(UserRefreshTokenRepository userRefreshTokenRepository) {
		this.userRefreshTokenRepository = userRefreshTokenRepository;
	}

	@Transactional
	public void save(String rawToken, User user, String deviceInfo, String ipAddress) {
		UserRefreshToken refreshToken = new UserRefreshToken();
		refreshToken.setToken(hashToken(rawToken));
		refreshToken.setUser(user);
		refreshToken.setExpiresAt(Instant.now().plusMillis(refreshTokenExpiration));
		refreshToken.setDeviceInfo(deviceInfo);
		refreshToken.setIpAddress(ipAddress);
		refreshToken.setRevoked(false);
		userRefreshTokenRepository.save(refreshToken);
	}

	public UserRefreshToken validateForRefresh(String rawRefreshToken) {
		UserRefreshToken storedToken = userRefreshTokenRepository.findByToken(hashToken(rawRefreshToken))
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
	public void revoke(UserRefreshToken refreshToken) {
		refreshToken.setRevoked(true);
		userRefreshTokenRepository.save(refreshToken);
	}

	@Transactional
	public void revokeAllActiveByEmail(String email) {
		userRefreshTokenRepository.revokeAllActiveByEmail(email);
	}

	@Transactional
	public void revokeSessionByRawToken(String rawRefreshToken) {
		userRefreshTokenRepository.findByToken(hashToken(rawRefreshToken)).ifPresent(stored -> {
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
		} catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException("SHA-256 không khả dụng", ex);
		}
	}
}
