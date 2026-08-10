package com.recruitment.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.recruitment.dto.response.AdminUserResponse;
import com.recruitment.entity.AdminUser;
import com.recruitment.exception.ResourceNotFoundException;
import com.recruitment.mapper.AdminUserMapper;
import com.recruitment.repository.AdminUserRepository;

@Service
public class AdminUserService {

	private final AdminUserRepository adminUserRepository;
	private final AdminUserMapper adminUserMapper;

	public AdminUserService(AdminUserRepository adminUserRepository, AdminUserMapper adminUserMapper) {
		this.adminUserRepository = adminUserRepository;
		this.adminUserMapper = adminUserMapper;
	}

	@Transactional(readOnly = true)
	public AdminUser getByUsername(String username) {
		return adminUserRepository.findByUsername(username)
				.orElseThrow(() -> new ResourceNotFoundException("error.resource.admin", "username", username));
	}

	@Transactional(readOnly = true)
	public AdminUserResponse getMe(String username) {
		return adminUserMapper.toResponse(getByUsername(username));
	}
}
