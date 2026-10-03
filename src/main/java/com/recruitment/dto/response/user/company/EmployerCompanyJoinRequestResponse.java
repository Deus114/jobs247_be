package com.recruitment.dto.response.user.company;

import java.time.Instant;

public record EmployerCompanyJoinRequestResponse(
		Long id,
		Long companyId,
		String companyName,
		String companyLogo,
		String companyStatus,
		Long userId,
		String userName,
		String userEmail,
		String userAvatar,
		String message,
		String status,
		Instant createdAt,
		Instant updatedAt) {
}
