package com.recruitment.dto.request.common;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UnregisterDeviceTokenRequest(
		@NotBlank(message = "{validation.device-token.token.required}") @Size(max = 512, message = "{validation.device-token.token.max-length}") String token) {
}
