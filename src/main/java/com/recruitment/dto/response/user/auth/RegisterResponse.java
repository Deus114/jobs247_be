package com.recruitment.dto.response.user.auth;

import java.time.Instant;
import java.util.List;

import com.recruitment.dto.response.user.company.UserCompanyMembershipResponse;

public record RegisterResponse(
		Long id,
		String name,
		String email,
		String type,
		boolean emailVerified,
		String avatar,
		String phone,
		List<UserCompanyMembershipResponse> companies,
		Instant createdAt,
		Instant updatedAt) {
}
