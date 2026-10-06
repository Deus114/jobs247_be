package com.recruitment.dto.request.admin.job;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;

public record AdminJobFilterRequest(
		String keyword,
		@Schema(description = "PENDING, APPROVED, REJECTED")
		String status,
		Long companyId,
		Long industryId,
		Long provinceId,
		String employmentType,
		String experienceLevel,
		Boolean active,
		Boolean hot,
		LocalDate fromDate,
		LocalDate toDate,
		Boolean deleted) {

	public boolean isDeleted() {
		return Boolean.TRUE.equals(deleted);
	}
}
