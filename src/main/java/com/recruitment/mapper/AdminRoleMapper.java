package com.recruitment.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.recruitment.dto.response.admin.AdminPermissionResponse;
import com.recruitment.dto.response.admin.AdminRoleResponse;
import com.recruitment.entity.AdminPermission;
import com.recruitment.entity.AdminRole;

@Component
public class AdminRoleMapper {

	private final AdminPermissionMapper adminPermissionMapper;

	public AdminRoleMapper(AdminPermissionMapper adminPermissionMapper) {
		this.adminPermissionMapper = adminPermissionMapper;
	}

	public AdminRoleResponse toResponse(AdminRole role) {
		return new AdminRoleResponse(
				role.getId(),
				role.getName(),
				role.getDescription(),
				role.isFullAccess(),
				role.isActive(),
				toPermissionResponses(role.getPermissions()));
	}

	public List<AdminPermissionResponse> toPermissionResponses(List<AdminPermission> permissions) {
		return permissions.stream()
				.map(adminPermissionMapper::toResponse)
				.toList();
	}
}
