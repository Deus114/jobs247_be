package com.recruitment.dto.request.user.application;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;

public record EmployerApplicationFilterRequest(
		String keyword,
		String status,
		@Schema(description = "Filter theo công ty (phải là công ty employer đang quản lý)")
		Long companyId,
		Long jobId,
		LocalDate fromDate,
		LocalDate toDate) {
}
