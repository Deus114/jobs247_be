package com.recruitment.dto.response.admin;

import java.util.List;

public record AdminRoleResponse(
		Long id,
		String name,
		String description,
		boolean fullAccess,
		boolean active,
		List<AdminPermissionResponse> permissions) {
}
