package com.recruitment.dto.request.user.job;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;

public record EmployerJobFilterRequest(
		String keyword,
		@Schema(description = "Filter theo công ty (phải là công ty employer đang quản lý)")
		Long companyId,
		@Schema(description = "PENDING, APPROVED, REJECTED")
		String status,
		Long industryId,
		Long provinceId,
		String employmentType,
		String experienceLevel,
		Boolean active,
		Boolean hot,
		LocalDate fromDate,
		LocalDate toDate,
		@Schema(description = "Lương từ, cùng đơn vị với salaryMin/salaryMax. Bỏ trống nếu không giới hạn đầu dưới.") Integer salaryFrom,
		@Schema(description = "Lương đến, cùng đơn vị với salaryMin/salaryMax. Bỏ trống nếu không giới hạn đầu trên.") Integer salaryTo,
		Boolean deleted) {

	public boolean isDeleted() {
		return Boolean.TRUE.equals(deleted);
	}
}
