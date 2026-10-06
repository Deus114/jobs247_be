package com.recruitment.util.helper;

import org.springframework.security.oauth2.jwt.Jwt;

import com.recruitment.security.JwtTokenProvider;

public final class OptionalJwtHelper {

	private OptionalJwtHelper() {
	}

	public static Long userId(Jwt jwt) {
		if (jwt == null
				|| !JwtTokenProvider.USER_ACCOUNT.equals(jwt.getClaimAsString("account"))
				|| !JwtTokenProvider.USER_ACCESS_TYPE.equals(jwt.getClaimAsString("type"))) {
			return null;
		}
		Object claim = jwt.getClaim("userId");
		if (claim instanceof Number number) {
			return number.longValue();
		}
		return null;
	}
}
