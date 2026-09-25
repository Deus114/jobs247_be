package com.recruitment.dto.response.user.auth;

import java.time.Instant;

public record RegisterResponse(
		Long id,
		String name,
		String email,
		String type,
		boolean emailVerified,
		Instant createdAt,
		Instant updatedAt) {
}
