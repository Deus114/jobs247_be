package com.recruitment.dto.request.admin.role;

import com.recruitment.util.helper.AccentHelper;

public record AdminRoleFilterRequest(
		String keyword,
		Boolean fullAccess,
		Boolean active,
		Boolean deleted) {

	public boolean isDeleted() {
		return Boolean.TRUE.equals(deleted);
	}

	public String normalizedKeyword() {
		return AccentHelper.normalizeForSearch(keyword);
	}
}
