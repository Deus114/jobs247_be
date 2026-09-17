package com.recruitment.dto.request.admin.industrygroup;

import com.recruitment.util.helper.AccentHelper;

public record IndustryGroupFilterRequest(
		String keyword,
		Boolean active,
		Boolean deleted) {

	public boolean isDeleted() {
		return Boolean.TRUE.equals(deleted);
	}

	public String normalizedKeyword() {
		return AccentHelper.normalizeForSearch(keyword);
	}
}
