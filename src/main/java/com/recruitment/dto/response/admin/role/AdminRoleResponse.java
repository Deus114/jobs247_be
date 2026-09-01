package com.recruitment.dto.response.admin.role;

import java.util.List;

import com.recruitment.dto.response.admin.permission.AdminPermissionResponse;

public record AdminRoleResponse(
		Long id,
		String name,
		String description,
		boolean fullAccess,
		boolean active,
		List<AdminPermissionResponse> permissions) {
}
