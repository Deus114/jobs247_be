package com.recruitment.dto.request.admin.permission;

import com.recruitment.enums.PermissionTypeEnum;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateAdminPermissionRequest(
		@NotBlank(message = "{validation.permission-name.required}")
		@Size(max = 150, message = "{validation.permission-name.max-length}")
		String name,

		@Size(max = 255, message = "{validation.description.max-length}")
		String description,

		@NotBlank(message = "{validation.module.required}")
		@Size(max = 100, message = "{validation.module.max-length}")
		String module,

		@NotBlank(message = "{validation.path.required}")
		@Size(max = 255, message = "{validation.path.max-length}")
		String path,

		@NotBlank(message = "{validation.method.required}")
		@Size(max = 10, message = "{validation.method.max-length}")
		String method,

		@NotNull(message = "{validation.permission-type.required}")
		PermissionTypeEnum type,

		Boolean active) {
}
