package com.recruitment.dto.response.admin.user;

import java.time.Instant;

import com.recruitment.dto.response.admin.role.AdminRoleResponse;

public record AdminUserResponse(
		Long id,
		String username,
		String name,
		String avatar,
		AdminRoleResponse role,
		boolean active,
		boolean pushEnabled,
		Instant createdAt,
		Instant updatedAt) {
}
