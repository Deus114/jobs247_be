package com.recruitment.dto.request.admin.permission;

import com.recruitment.enums.PermissionTypeEnum;

public record AdminPermissionFilterRequest(
		String keyword,
		String module,
		String method,
		PermissionTypeEnum type,
		Boolean active,
		Boolean deleted) {

	public boolean isDeleted() {
		return Boolean.TRUE.equals(deleted);
	}

	public String normalizedModule() {
		if (module == null || module.isBlank()) {
			return null;
		}
		return module.trim();
	}

	public String normalizedMethod() {
		if (method == null || method.isBlank()) {
			return null;
		}
		return method.trim().toUpperCase();
	}
}
