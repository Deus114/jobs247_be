package com.recruitment.dto.response.admin.job;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import com.recruitment.dto.response.common.JobCatalogItemResponse;

public record AdminJobResponse(
		Long id,
		Long companyId,
		String companyName,
		String companyLogo,
		Long createdByUserId,
		String createdByUserName,
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
		long applicationCount,
		String status,
		String rejectionReason,
		Instant rejectedAt,
		Instant approvedAt,
		boolean active,
		Instant createdAt,
		Instant updatedAt) {
}
