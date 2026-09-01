package com.recruitment.dto.request.admin.permission;

import com.recruitment.enums.PermissionTypeEnum;

import jakarta.validation.constraints.Size;

public record UpdateAdminPermissionRequest(
		@Size(max = 150, message = "{validation.permission-name.max-length}")
		String name,

		@Size(max = 255, message = "{validation.description.max-length}")
		String description,

		@Size(max = 100, message = "{validation.module.max-length}")
		String module,

		@Size(max = 255, message = "{validation.path.max-length}")
		String path,

		@Size(max = 10, message = "{validation.method.max-length}")
		String method,

		PermissionTypeEnum type,

		Boolean active) {
}
