package com.recruitment.mapper;

import org.springframework.stereotype.Component;

import com.recruitment.dto.response.admin.AdminPermissionResponse;
import com.recruitment.entity.AdminPermission;

@Component
public class AdminPermissionMapper {

	public AdminPermissionResponse toResponse(AdminPermission permission) {
		return new AdminPermissionResponse(
				permission.getId(),
				permission.getName(),
				permission.getDescription(),
				permission.getModule(),
				permission.getPath(),
				permission.getMethod(),
				permission.getType(),
				permission.isActive());
	}
}
