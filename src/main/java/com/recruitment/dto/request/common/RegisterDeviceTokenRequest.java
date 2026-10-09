package com.recruitment.dto.request.common;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;

public record RegisterDeviceTokenRequest(
		@NotBlank(message = "{validation.device-token.token.required}") @Size(max = 512, message = "{validation.device-token.token.max-length}") String token,

		@Schema(description = "WEB, ANDROID, IOS") @Size(max = 20, message = "{validation.device-token.platform.max-length}") String platform,

		@Size(max = 255, message = "{validation.device-token.device-info.max-length}") String deviceInfo) {
}
