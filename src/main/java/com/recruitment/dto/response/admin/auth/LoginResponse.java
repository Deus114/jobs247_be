package com.recruitment.dto.response.admin.auth;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.recruitment.dto.response.admin.user.AdminUserResponse;

@JsonPropertyOrder({ "user", "accessToken", "refreshToken" })
public record LoginResponse(
		String accessToken,
		String refreshToken,
		AdminUserResponse user) {
}
