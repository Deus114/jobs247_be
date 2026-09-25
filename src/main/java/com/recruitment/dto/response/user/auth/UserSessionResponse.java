package com.recruitment.dto.response.user.auth;

import java.time.Instant;

public record UserSessionResponse(
		Long id,
		String name,
		String email,
		String type,
		boolean emailVerified,
		boolean active,
		Instant createdAt,
		Instant updatedAt) {
}
