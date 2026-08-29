package com.recruitment.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.recruitment.dto.request.admin.AdminUserFilterRequest;
import com.recruitment.dto.request.admin.CreateAdminUserRequest;
import com.recruitment.dto.request.admin.UpdateAdminUserRequest;
import com.recruitment.dto.request.admin.UpdateMeRequest;
import com.recruitment.dto.response.admin.AdminUserResponse;
import com.recruitment.dto.response.common.ApiResponse;
import com.recruitment.dto.response.common.PaginatedResponse;
import com.recruitment.dto.response.common.UploadedFileResponse;
import com.recruitment.entity.AdminRole;
import com.recruitment.entity.AdminUser;
import com.recruitment.enums.UploadFolderType;
import com.recruitment.exception.BusinessRuleException;
import com.recruitment.exception.DuplicateResourceException;
import com.recruitment.exception.ResourceNotFoundException;
import com.recruitment.mapper.AdminUserMapper;
import com.recruitment.repository.AdminRoleRepository;
import com.recruitment.repository.AdminUserRepository;
import com.recruitment.specification.AdminUserSpecification;
import com.recruitment.util.helper.FileHelper;

@Service
public class AdminUserService {

	private final AdminUserRepository adminUserRepository;
	private final AdminRoleRepository adminRoleRepository;
	private final AdminUserMapper adminUserMapper;
	private final PasswordEncoder passwordEncoder;
	private final MessageService messageService;
	private final FileStorageService fileStorageService;

	public AdminUserService(
			AdminUserRepository adminUserRepository,
			AdminRoleRepository adminRoleRepository,
			AdminUserMapper adminUserMapper,
			PasswordEncoder passwordEncoder,
			MessageService messageService,
			FileStorageService fileStorageService) {
		this.adminUserRepository = adminUserRepository;
		this.adminRoleRepository = adminRoleRepository;
		this.adminUserMapper = adminUserMapper;
		this.passwordEncoder = passwordEncoder;
		this.messageService = messageService;
		this.fileStorageService = fileStorageService;
	}

	public AdminUser getByUsername(String username) {
		return adminUserRepository.findByUsername(username)
				.orElseThrow(() -> new ResourceNotFoundException("error.resource.admin", "username", username));
	}

	public AdminUser getByUsernameWithRolePermissions(String username) {
		return adminUserRepository.findByUsernameWithRolePermissions(username)
				.orElseThrow(() -> new ResourceNotFoundException("error.resource.admin", "username", username));
	}

	public ResponseEntity<ApiResponse<AdminUserResponse>> getMe(String username) {
		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("common.success"),
				adminUserMapper.toResponse(getByUsernameWithRolePermissions(username))));
	}

	public ResponseEntity<ApiResponse<PaginatedResponse<AdminUserResponse>>> getList(
			AdminUserFilterRequest filter,
			Pageable pageable) {

		Page<AdminUserResponse> resultPage = filter.isDeleted()
				? adminUserRepository.findAllSoftDeleted(
						filter.normalizedKeyword(),
						filter.roleId(),
						filter.active(),
						toNativePageable(pageable))
						.map(adminUserMapper::toResponse)
				: adminUserRepository.findAll(AdminUserSpecification.build(filter), pageable)
						.map(adminUserMapper::toResponse);

		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("admin-user.list.success"),
				PaginatedResponse.fromPage(resultPage)));
	}

	public ResponseEntity<ApiResponse<AdminUserResponse>> getById(Long id) {
		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("common.success"),
				toUserResponse(id)));
	}

	@Transactional
	public ResponseEntity<ApiResponse<AdminUserResponse>> create(
			CreateAdminUserRequest request,
			String currentUsername) {

		validateUsernameUnique(request.username(), null);

		AdminRole role = findRoleById(request.roleId());
		AdminUser adminUser = new AdminUser();
		adminUser.setUsername(request.username().trim());
		adminUser.setPassword(passwordEncoder.encode(request.password()));
		adminUser.setName(request.name().trim());
		adminUser.setRole(role);
		adminUser.setActive(request.active() == null || request.active());
		adminUser.setCreatedBy(currentUsername);
		adminUser.setUpdatedBy(currentUsername);

		MultipartFile avatar = request.avatar();
		if (FileHelper.hasFile(avatar)) {
			UploadedFileResponse uploaded = fileStorageService.uploadImage(avatar, UploadFolderType.ADMIN_AVATAR);
			adminUser.setAvatar(uploaded.url());
		}

		AdminUser saved = adminUserRepository.save(adminUser);

		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(
				HttpStatus.CREATED.value(),
				messageService.get("admin-user.create.success"),
				toUserResponse(saved.getId())));
	}

	@Transactional
	public ResponseEntity<ApiResponse<AdminUserResponse>> updateMe(
			String username,
			UpdateMeRequest request) {

		AdminUser adminUser = getByUsername(username);

		if (StringUtils.hasText(request.name())) {
			adminUser.setName(request.name().trim());
		}
		applyPasswordChange(adminUser, request.currentPassword(), request.newPassword());
		adminUser.setUpdatedBy(username);

		String oldAvatarUrl = adminUser.getAvatar();
		MultipartFile avatar = request.avatar();
		if (FileHelper.hasFile(avatar)) {
			UploadedFileResponse uploaded = fileStorageService.uploadImage(avatar, UploadFolderType.ADMIN_AVATAR);
			adminUser.setAvatar(uploaded.url());
		}

		AdminUser saved = adminUserRepository.save(adminUser);

		if (FileHelper.hasFile(avatar) && oldAvatarUrl != null && !oldAvatarUrl.equals(saved.getAvatar())) {
			fileStorageService.deleteByPublicUrl(oldAvatarUrl);
		}

		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("auth.me.update.success"),
				toUserResponse(saved.getId())));
	}

	@Transactional
	public ResponseEntity<ApiResponse<AdminUserResponse>> update(
			Long id,
			UpdateAdminUserRequest request,
			String currentUsername) {

		AdminUser adminUser = findActiveById(id);

		if (StringUtils.hasText(request.username())) {
			validateUsernameUnique(request.username(), id);
			adminUser.setUsername(request.username().trim());
		}
		if (StringUtils.hasText(request.name())) {
			adminUser.setName(request.name().trim());
		}
		if (request.roleId() != null) {
			adminUser.setRole(findRoleById(request.roleId()));
		}
		if (request.active() != null) {
			adminUser.setActive(request.active());
		}
		applyPasswordChange(adminUser, request.currentPassword(), request.newPassword());
		adminUser.setUpdatedBy(currentUsername);

		String oldAvatarUrl = adminUser.getAvatar();
		MultipartFile avatar = request.avatar();
		if (FileHelper.hasFile(avatar)) {
			UploadedFileResponse uploaded = fileStorageService.uploadImage(avatar, UploadFolderType.ADMIN_AVATAR);
			adminUser.setAvatar(uploaded.url());
		}

		AdminUser saved = adminUserRepository.save(adminUser);

		if (FileHelper.hasFile(avatar) && oldAvatarUrl != null && !oldAvatarUrl.equals(saved.getAvatar())) {
			fileStorageService.deleteByPublicUrl(oldAvatarUrl);
		}

		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("admin-user.update.success"),
				toUserResponse(saved.getId())));
	}

	@Transactional
	public ResponseEntity<ApiResponse<Void>> softDelete(Long id, String currentUsername) {
		AdminUser adminUser = findActiveById(id);
		adminUser.setDeletedBy(currentUsername);
		adminUserRepository.save(adminUser);
		adminUserRepository.delete(adminUser);

		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("admin-user.soft-delete.success"),
				null));
	}

	@Transactional
	public ResponseEntity<ApiResponse<Void>> restore(Long id, String currentUsername) {
		int restored = adminUserRepository.restoreById(id);
		if (restored == 0) {
			throw new ResourceNotFoundException("error.resource.admin", "id", id);
		}

		AdminUser adminUser = findActiveById(id);
		adminUser.setUpdatedBy(currentUsername);
		adminUserRepository.save(adminUser);

		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("admin-user.restore.success"),
				null));
	}

	@Transactional
	public ResponseEntity<ApiResponse<Void>> permanentDelete(Long id) {
		AdminUser softDeleted = adminUserRepository.findSoftDeletedById(id)
				.orElseThrow(() -> new BusinessRuleException("error.admin-user.not-soft-deleted", id));

		String avatarUrl = softDeleted.getAvatar();

		adminUserRepository.deleteRefreshTokensByAdminUserId(id);
		adminUserRepository.hardDeleteById(id);

		if (avatarUrl != null && !avatarUrl.isBlank()) {
			fileStorageService.deleteByPublicUrl(avatarUrl);
		}

		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("admin-user.permanent-delete.success"),
				null));
	}

	private void applyPasswordChange(AdminUser adminUser, String currentPassword, String newPassword) {
		boolean changingPassword = StringUtils.hasText(newPassword);
		if (!changingPassword) {
			if (StringUtils.hasText(currentPassword)) {
				throw new BusinessRuleException("error.admin-user.password.new-required");
			}
			return;
		}

		if (!StringUtils.hasText(currentPassword)) {
			throw new BusinessRuleException("error.admin-user.password.current-required");
		}
		if (newPassword.trim().length() < 6 || newPassword.trim().length() > 100) {
			throw new BusinessRuleException("error.admin-user.password.length");
		}
		if (!passwordEncoder.matches(currentPassword, adminUser.getPassword())) {
			throw new BusinessRuleException("error.admin-user.password.current-invalid");
		}
		adminUser.setPassword(passwordEncoder.encode(newPassword.trim()));
	}

	private AdminUser findActiveById(Long id) {
		return adminUserRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("error.resource.admin", "id", id));
	}

	private AdminUser findActiveByIdWithRolePermissions(Long id) {
		return adminUserRepository.findByIdWithRolePermissions(id)
				.orElseThrow(() -> new ResourceNotFoundException("error.resource.admin", "id", id));
	}

	private AdminUserResponse toUserResponse(Long id) {
		return adminUserMapper.toResponse(findActiveByIdWithRolePermissions(id));
	}

	private AdminRole findRoleById(Long roleId) {
		return adminRoleRepository.findById(roleId)
				.orElseThrow(() -> new ResourceNotFoundException("error.resource.role", "id", roleId));
	}

	private void validateUsernameUnique(String username, Long excludeId) {
		boolean exists = excludeId == null
				? adminUserRepository.existsByUsername(username.trim())
				: adminUserRepository.existsByUsernameAndIdNot(username.trim(), excludeId);
		if (exists) {
			throw new DuplicateResourceException("error.admin-user.username.duplicate", username.trim());
		}
	}

	/**
	 * Native soft-delete query already has ORDER BY created_at. Use page/size only to avoid Spring Sort property names
	 * (createdAt) on SQL columns.
	 */
	private Pageable toNativePageable(Pageable pageable) {
		return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
	}
}
