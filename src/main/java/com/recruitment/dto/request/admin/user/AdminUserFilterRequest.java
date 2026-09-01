package com.recruitment.dto.request.admin.user;

public record AdminUserFilterRequest(
		String keyword,
		Long roleId,
		Boolean active,
		Boolean deleted) {

	public boolean isDeleted() {
		return Boolean.TRUE.equals(deleted);
	}

	public String normalizedKeyword() {
		if (keyword == null || keyword.isBlank()) {
			return null;
		}
		return keyword.trim();
	}
}
