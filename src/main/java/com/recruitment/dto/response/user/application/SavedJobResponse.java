package com.recruitment.dto.response.user.application;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import com.recruitment.dto.response.common.JobCatalogItemResponse;

public record SavedJobResponse(
		Long id,
		Long jobId,
		String jobTitle,
		Long companyId,
		String companyName,
		String companyLogo,
		List<JobCatalogItemResponse> provinces,
		List<String> employmentTypes,
		List<String> experienceLevels,
		Integer experienceYears,
		Integer salaryMin,
		Integer salaryMax,
		boolean salaryNegotiable,
		LocalDate deadline,
		boolean hot,
		boolean jobActive,
		String jobStatus,
		boolean applied,
		long applicationCount,
		Instant savedAt) {
}
