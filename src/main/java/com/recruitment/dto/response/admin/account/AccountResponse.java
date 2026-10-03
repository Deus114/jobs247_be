package com.recruitment.dto.response.admin.account;

import java.time.Instant;
import java.util.List;

public record AccountResponse(
		Long id,
		String name,
		String email,
		String type,
		boolean emailVerified,
		String avatar,
		String phone,
		boolean active,
		List<AccountCompanyMembershipResponse> companies,
		Instant createdAt,
		Instant updatedAt) {
}
