package com.recruitment.dto.response.admin.permission;

import com.recruitment.enums.PermissionTypeEnum;

public record AdminPermissionResponse(
		Long id,
		String name,
		String description,
		String module,
		String path,
		String method,
		PermissionTypeEnum type,
		boolean active) {
}
