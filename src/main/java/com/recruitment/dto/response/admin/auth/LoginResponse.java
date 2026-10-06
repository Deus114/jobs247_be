package com.recruitment.dto.response.admin.auth;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.recruitment.dto.response.admin.user.AdminUserResponse;

@JsonPropertyOrder({ "user", "accessToken" })
public record LoginResponse(
		String accessToken,
		AdminUserResponse user) {
}
