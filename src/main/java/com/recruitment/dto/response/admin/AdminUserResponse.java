package com.recruitment.dto.response.admin;

import java.time.Instant;

public record AdminUserResponse(
		Long id,
		String username,
		String name,
		String avatar,
		AdminRoleResponse role,
		boolean active,
		Instant createdAt,
		Instant updatedAt) {
}
