package com.recruitment.dto.response.user.auth;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "user", "accessToken", "refreshToken" })
public record UserLoginResponse(
		String accessToken,
		String refreshToken,
		UserSessionResponse user) {
}
