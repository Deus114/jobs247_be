package com.recruitment.dto.request.user.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserLoginRequest(
		@NotBlank(message = "{validation.email.required}")
		@Email(message = "{validation.email.invalid}")
		@Size(max = 255, message = "{validation.email.max-length}")
		String email,

		@NotBlank(message = "{validation.password.required}")
		String password) {
}
