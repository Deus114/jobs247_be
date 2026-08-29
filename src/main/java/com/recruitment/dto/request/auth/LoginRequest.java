package com.recruitment.dto.request.auth;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
		@NotBlank(message = "{validation.username.required}")
		String userName,

		@NotBlank(message = "{validation.password.required}")
		String password) {
}
