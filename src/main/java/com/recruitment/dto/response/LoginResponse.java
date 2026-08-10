package com.recruitment.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "accessToken", "refreshToken", "user" })
public record LoginResponse(
		String accessToken,
		String refreshToken,
		AdminUserResponse user) {
}
