package com.recruitment.dto.request.common;

public record PublicJobFilterRequest(
		String keyword,
		Long companyId,
		Long industryId,
		Long provinceId,
		Long educationLevelId,
		String employmentType,
		String experienceLevel,
		Boolean hot) {
}
