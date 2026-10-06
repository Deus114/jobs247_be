package com.recruitment.dto.request.admin.application;

import java.time.LocalDate;

public record AdminApplicationFilterRequest(
		String keyword,
		String status,
		Long companyId,
		Long jobId,
		LocalDate fromDate,
		LocalDate toDate) {
}
