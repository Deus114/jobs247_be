package com.recruitment.security;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import com.recruitment.entity.AdminUser;
import com.recruitment.entity.User;
import com.recruitment.exception.InvalidTokenException;

@Component
public class JwtTokenProvider {

	public static final String EMAIL_VERIFICATION_TYPE = "email_verification";
	public static final String USER_ACCESS_TYPE = "user_access";
	public static final String USER_REFRESH_TYPE = "user_refresh";
	public static final String USER_ACCOUNT = "user";

	private final JwtEncoder jwtEncoder;
	private final JwtDecoder jwtDecoder;

	@Value("${jwt.access-token-expiration}")
	private long accessTokenExpiration;

	@Value("${jwt.refresh-expiration}")
	private long refreshTokenExpiration;

	@Value("${jwt.email-verification-expiration:900000}")
	private long emailVerificationExpiration;

	public JwtTokenProvider(JwtEncoder jwtEncoder, JwtDecoder jwtDecoder) {
		this.jwtEncoder = jwtEncoder;
		this.jwtDecoder = jwtDecoder;
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

	public String generateEmailVerificationToken(String email) {
		Instant now = Instant.now();
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.subject(email.trim().toLowerCase())
				.issuedAt(now)
				.expiresAt(now.plusMillis(emailVerificationExpiration))
				.claim("type", EMAIL_VERIFICATION_TYPE)
				.claim("purpose", "register")
				.build();
		return encode(claims);
	}

	public String requireRegisterVerifiedEmail(String token) {
		try {
			Jwt jwt = jwtDecoder.decode(token);
			if (!EMAIL_VERIFICATION_TYPE.equals(jwt.getClaimAsString("type"))
					|| !"register".equals(jwt.getClaimAsString("purpose"))) {
				throw new InvalidTokenException("error.auth.verification-token.invalid");
			}
			String email = jwt.getSubject();
			if (email == null || email.isBlank()) {
				throw new InvalidTokenException("error.auth.verification-token.invalid");
			}
			return email;
		} catch (InvalidTokenException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new InvalidTokenException("error.auth.verification-token.invalid");
		}
	}

	public String generateUserAccessToken(User user) {
		Instant now = Instant.now();
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.subject(user.getEmail())
				.issuedAt(now)
				.expiresAt(now.plusMillis(accessTokenExpiration))
				.claim("userId", user.getId())
				.claim("roles", List.of("ROLE_" + user.getType()))
				.claim("account", USER_ACCOUNT)
				.claim("type", USER_ACCESS_TYPE)
				.build();
		return encode(claims);
	}

	public String generateUserRefreshToken(User user) {
		Instant now = Instant.now();
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.id(UUID.randomUUID().toString())
				.subject(user.getEmail())
				.issuedAt(now)
				.expiresAt(now.plusMillis(refreshTokenExpiration))
				.claim("userId", user.getId())
				.claim("account", USER_ACCOUNT)
				.claim("type", USER_REFRESH_TYPE)
				.build();
		return encode(claims);
	}

	public String requireUserRefreshEmail(String token) {
		try {
			Jwt jwt = jwtDecoder.decode(token);
			if (!USER_REFRESH_TYPE.equals(jwt.getClaimAsString("type"))
					|| !USER_ACCOUNT.equals(jwt.getClaimAsString("account"))) {
				throw new InvalidTokenException("error.refresh-token.invalid");
			}
			String email = jwt.getSubject();
			if (email == null || email.isBlank()) {
				throw new InvalidTokenException("error.refresh-token.invalid");
			}
			return email;
		} catch (InvalidTokenException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new InvalidTokenException("error.refresh-token.invalid");
		}
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
