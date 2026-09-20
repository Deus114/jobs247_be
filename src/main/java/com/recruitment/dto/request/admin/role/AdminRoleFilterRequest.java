package com.recruitment.dto.request.admin.role;

public record AdminRoleFilterRequest(
		String keyword,
		Boolean fullAccess,
		Boolean active,
		Boolean deleted) {

	public boolean isDeleted() {
		return Boolean.TRUE.equals(deleted);
	}
}
