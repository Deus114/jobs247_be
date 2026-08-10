package com.recruitment.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import com.recruitment.entity.AdminUser;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class JwtTokenProvider {

	private final JwtEncoder jwtEncoder;

	@Value("${jwt.access-token-expiration}")
	private long accessTokenExpiration;

	@Value("${jwt.refresh-expiration}")
	private long refreshTokenExpiration;

	public JwtTokenProvider(JwtEncoder jwtEncoder) {
		this.jwtEncoder = jwtEncoder;
	}

	public String generateAccessToken(AdminUser adminUser) {
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

		return encode(claims);
	}

	public String generateRefreshToken(AdminUser adminUser) {
		Instant now = Instant.now();

		JwtClaimsSet claims = JwtClaimsSet.builder()
				.id(UUID.randomUUID().toString())
				.subject(adminUser.getUsername())
				.issuedAt(now)
				.expiresAt(now.plusMillis(refreshTokenExpiration))
				.claim("userId", adminUser.getId())
				.claim("type", "refresh")
				.build();

		return encode(claims);
	}

	private String encode(JwtClaimsSet claims) {
		JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
		return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
	}
}
