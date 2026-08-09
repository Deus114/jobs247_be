package com.recruitment.dto.response;

import java.time.Instant;

public record AdminUserResponse(
		Long id,
		String username,
		String name,
		String roleName,
		boolean roleFullAccess,
		boolean active,
		Instant createdAt,
		Instant updatedAt) {
}
