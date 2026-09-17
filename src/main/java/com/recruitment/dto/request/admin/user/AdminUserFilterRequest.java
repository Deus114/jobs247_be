package com.recruitment.dto.request.admin.user;

import com.recruitment.util.helper.AccentHelper;

public record AdminUserFilterRequest(
		String keyword,
		Long roleId,
		Boolean active,
		Boolean deleted) {

	public boolean isDeleted() {
		return Boolean.TRUE.equals(deleted);
	}

	public String normalizedKeyword() {
		return AccentHelper.normalizeForSearch(keyword);
	}
}
