package com.recruitment.dto.response.common;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record PublicJobResponse(
		Long id,
		String slug,
		Long companyId,
		String companyName,
		String companySlug,
		String companyLogo,
		String companyAddress,
		String title,
		List<JobCatalogItemResponse> industries,
		List<JobCatalogItemResponse> educationLevels,
		List<JobCatalogItemResponse> provinces,
		List<String> employmentTypes,
		List<String> experienceLevels,
		Integer experienceYears,
		Integer salaryMin,
		Integer salaryMax,
		boolean salaryNegotiable,
		LocalDate deadline,
		String workLocation,
		String workingTime,
		String description,
		String requirements,
		String benefits,
		String applicantQuestion,
		boolean hot,
		boolean applied,
		boolean saved,
		long applicationCount,
		Instant createdAt,
		Instant updatedAt) {
}
