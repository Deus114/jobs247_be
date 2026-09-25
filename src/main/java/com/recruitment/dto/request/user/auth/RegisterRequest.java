package com.recruitment.dto.request.user.auth;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
		@NotBlank(message = "{validation.name.required}")
		@Size(max = 255, message = "{validation.name.max-length}")
		String name,

		@NotBlank(message = "{validation.password.required}")
		@Size(min = 6, max = 100, message = "{validation.password.length}")
		String password,

		@NotBlank(message = "{validation.password.confirm.required}")
		String confirmPassword,

		@NotBlank(message = "{validation.user-type.required}")
		@Size(max = 20, message = "{validation.user-type.max-length}")
		String type,

		@NotBlank(message = "{validation.verification-token.required}")
		String verificationToken,

		@AssertTrue(message = "{validation.terms.required}")
		boolean acceptTerms) {
}
