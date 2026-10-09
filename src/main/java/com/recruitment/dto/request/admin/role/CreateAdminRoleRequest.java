package com.recruitment.dto.request.admin.role;

import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateAdminRoleRequest(
		@NotBlank(message = "{validation.role-name.required}")
		@Size(max = 100, message = "{validation.role-name.max-length}")
		String name,

		@Size(max = 255, message = "{validation.description.max-length}")
		String description,

		Boolean fullAccess,

		Boolean active,

		List<Long> permissionIds) {
}
