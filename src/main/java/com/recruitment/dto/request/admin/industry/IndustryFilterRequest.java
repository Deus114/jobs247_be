package com.recruitment.dto.request.admin.industry;

import com.recruitment.util.helper.AccentHelper;

public record IndustryFilterRequest(
		String keyword,
		Boolean active,
		Boolean deleted,
		Long industryGroupId) {

	public boolean isDeleted() {
		return Boolean.TRUE.equals(deleted);
	}

	public String normalizedKeyword() {
		return AccentHelper.normalizeForSearch(keyword);
	}
}
