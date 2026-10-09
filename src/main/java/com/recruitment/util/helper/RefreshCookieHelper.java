package com.recruitment.util.helper;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class RefreshCookieHelper {

	private final boolean cookieSecure;
	private final long refreshExpirationMillis;

	public RefreshCookieHelper(
			@Value("${app.auth.cookie-secure:true}")
			boolean cookieSecure,
			@Value("${jwt.refresh-expiration}")
			long refreshExpirationMillis) {
		this.cookieSecure = cookieSecure;
		this.refreshExpirationMillis = refreshExpirationMillis;
	}

	public void set(HttpServletResponse response, String cookieName, String path, String rawToken) {
		ResponseCookie cookie = ResponseCookie.from(cookieName, rawToken)
				.httpOnly(true)
				.secure(cookieSecure)
				.sameSite("Lax")
				.path(path)
				.maxAge(Math.max(1, refreshExpirationMillis / 1000))
				.build();
		response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
	}

	public void clear(HttpServletResponse response, String cookieName, String path) {
		ResponseCookie cookie = ResponseCookie.from(cookieName, "")
				.httpOnly(true)
				.secure(cookieSecure)
				.sameSite("Lax")
				.path(path)
				.maxAge(0)
				.build();
		response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
	}
}
