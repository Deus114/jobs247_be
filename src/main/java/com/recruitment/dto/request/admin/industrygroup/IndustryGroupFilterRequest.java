package com.recruitment.dto.request.admin.industrygroup;

public record IndustryGroupFilterRequest(
		String keyword,
		Boolean active,
		Boolean deleted) {

	public boolean isDeleted() {
		return Boolean.TRUE.equals(deleted);
	}
}
