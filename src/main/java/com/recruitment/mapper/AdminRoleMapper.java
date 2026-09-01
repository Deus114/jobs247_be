package com.recruitment.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.recruitment.dto.response.admin.permission.AdminPermissionResponse;
import com.recruitment.dto.response.admin.role.AdminRoleResponse;
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
		if (permissions == null || permissions.isEmpty()) {
			return List.of();
		}
		return permissions.stream()
				.map(permission -> adminPermissionMapper.toResponse(permission))
				.toList();
	}
}
