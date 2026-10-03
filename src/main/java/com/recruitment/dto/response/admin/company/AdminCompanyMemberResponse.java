package com.recruitment.dto.response.admin.company;

import java.time.Instant;

public record AdminCompanyMemberResponse(
		Long id,
		Long userId,
		String name,
		String email,
		String phone,
		String avatar,
		String role,
		boolean active,
		Instant createdAt,
		Instant updatedAt) {
}
