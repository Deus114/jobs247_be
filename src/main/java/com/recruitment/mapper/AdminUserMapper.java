package com.recruitment.mapper;

import org.springframework.stereotype.Component;

import com.recruitment.dto.response.admin.user.AdminUserResponse;
import com.recruitment.entity.AdminUser;

@Component
public class AdminUserMapper {

	private final AdminRoleMapper adminRoleMapper;

	public AdminUserMapper(AdminRoleMapper adminRoleMapper) {
		this.adminRoleMapper = adminRoleMapper;
	}

	public AdminUserResponse toResponse(AdminUser adminUser) {
		return new AdminUserResponse(
				adminUser.getId(),
				adminUser.getUsername(),
				adminUser.getName(),
				adminUser.getAvatar(),
				adminRoleMapper.toResponse(adminUser.getRole()),
				adminUser.isActive(),
				adminUser.isPushEnabled(),
				adminUser.getCreatedAt(),
				adminUser.getUpdatedAt());
	}
}
