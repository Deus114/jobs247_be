package com.recruitment.dto.response.user.auth;

import java.time.Instant;
import java.util.List;

import com.recruitment.dto.response.user.company.UserCompanyMembershipResponse;

public record UserSessionResponse(
		Long id,
		String name,
		String email,
		String type,
		boolean emailVerified,
		String avatar,
		String phone,
		boolean active,
		boolean pushEnabled,
		List<UserCompanyMembershipResponse> companies,
		Instant createdAt,
		Instant updatedAt) {
}
