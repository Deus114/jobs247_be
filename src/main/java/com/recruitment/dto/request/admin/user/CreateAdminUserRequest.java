package com.recruitment.dto.request.admin.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.media.Schema;

public record CreateAdminUserRequest(
		@NotBlank(message = "{validation.username.required}")
		@Size(max = 100, message = "{validation.username.max-length}")
		String username,

		@NotBlank(message = "{validation.password.required}")
		@Size(min = 6, max = 100, message = "{validation.password.length}")
		String password,

		@NotBlank(message = "{validation.name.required}")
		@Size(max = 255, message = "{validation.name.max-length}")
		String name,

		@NotNull(message = "{validation.role-id.required}")
		Long roleId,

		Boolean active,

		@Schema(type = "string", format = "binary", description = "Ảnh avatar (tùy chọn)")
		MultipartFile avatar) {
}
