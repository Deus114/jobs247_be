package com.recruitment.dto.request.admin.educationlevel;

public record EducationLevelFilterRequest(
		String keyword,
		Boolean active,
		Boolean deleted) {

	public boolean isDeleted() {
		return Boolean.TRUE.equals(deleted);
	}
}
