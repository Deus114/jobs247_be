package com.recruitment.dto.request.admin.industry;

public record IndustryFilterRequest(
		String keyword,
		Long industryGroupId,
		Boolean active,
		Boolean deleted) {

	public boolean isDeleted() {
		return Boolean.TRUE.equals(deleted);
	}
}
