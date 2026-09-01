package com.recruitment.dto.request.admin.role;

import java.util.List;

import jakarta.validation.constraints.Size;

public record UpdateAdminRoleRequest(
		@Size(max = 100, message = "{validation.role-name.max-length}")
		String name,

		@Size(max = 255, message = "{validation.description.max-length}")
		String description,

		Boolean fullAccess,

		Boolean active,

		List<Long> permissionIds) {
}
