package com.recruitment.mapper;

import org.springframework.stereotype.Component;

import com.recruitment.dto.response.AdminUserResponse;
import com.recruitment.entity.AdminUser;

@Component
public class AdminUserMapper {

	public AdminUserResponse toResponse(AdminUser adminUser) {
		return new AdminUserResponse(
				adminUser.getId(),
				adminUser.getUsername(),
				adminUser.getName(),
				adminUser.getRole().getName(),
				adminUser.getRole().isFullAccess(),
				adminUser.isActive(),
				adminUser.getCreatedAt(),
				adminUser.getUpdatedAt());
	}
}
