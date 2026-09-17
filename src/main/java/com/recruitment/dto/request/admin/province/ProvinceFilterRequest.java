package com.recruitment.dto.request.admin.province;

import com.recruitment.util.helper.AccentHelper;

public record ProvinceFilterRequest(
		String keyword,
		String region,
		Boolean active,
		Boolean deleted) {

	public boolean isDeleted() {
		return Boolean.TRUE.equals(deleted);
	}

	public String normalizedKeyword() {
		return AccentHelper.normalizeForSearch(keyword);
	}

	public String normalizedRegion() {
		if (region == null || region.isBlank()) {
			return null;
		}
		return region.trim().toUpperCase();
	}
}
