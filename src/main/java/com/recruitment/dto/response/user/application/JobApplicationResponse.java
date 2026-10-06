package com.recruitment.dto.response.user.application;

import java.time.Instant;

public record JobApplicationResponse(
		Long id,
		Long jobId,
		String jobTitle,
		Long companyId,
		String companyName,
		Long userId,
		String fullName,
		String email,
		String phone,
		String cvUrl,
		String coverLetter,
		String status,
		Instant viewedAt,
		Long viewedByUserId,
		String viewedByUserName,
		Instant createdAt,
		Instant updatedAt) {
}
