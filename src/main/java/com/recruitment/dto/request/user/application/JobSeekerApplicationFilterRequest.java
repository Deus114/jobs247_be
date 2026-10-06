package com.recruitment.dto.request.user.application;

import java.time.LocalDate;

public record JobSeekerApplicationFilterRequest(
		String keyword,
		String status,
		Long jobId,
		LocalDate fromDate,
		LocalDate toDate) {
}
