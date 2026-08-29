package com.recruitment.dto.response.auth;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.recruitment.dto.response.admin.AdminUserResponse;

@JsonPropertyOrder({ "user", "accessToken", "refreshToken" })
public record LoginResponse(
		String accessToken,
		String refreshToken,
		AdminUserResponse user) {
}
