package com.recruitment.dto.response.user.auth;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "user", "accessToken" })
public record UserLoginResponse(
		String accessToken,
		UserSessionResponse user) {
}
