package com.recruitment.dto.request.admin.application;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;

public record AdminApplicationFilterRequest(
		String keyword,
		@Schema(description = "SUBMITTED, VIEWED")
		String status,
		Long companyId,
		Long jobId,
		LocalDate fromDate,
		LocalDate toDate,
		@Schema(description = "true = chỉ lấy đơn đã xóa mềm")
		Boolean deleted) {

	public boolean isDeleted() {
		return Boolean.TRUE.equals(deleted);
	}
}
