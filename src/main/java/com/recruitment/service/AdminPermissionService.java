package com.recruitment.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.recruitment.dto.request.admin.permission.AdminPermissionFilterRequest;
import com.recruitment.dto.request.admin.permission.CreateAdminPermissionRequest;
import com.recruitment.dto.request.admin.permission.UpdateAdminPermissionRequest;
import com.recruitment.dto.response.admin.permission.AdminPermissionResponse;
import com.recruitment.dto.response.common.ApiResponse;
import com.recruitment.dto.response.common.PaginatedResponse;
import com.recruitment.entity.AdminPermission;
import com.recruitment.exception.BusinessRuleException;
import com.recruitment.exception.DuplicateResourceException;
import com.recruitment.exception.ResourceNotFoundException;
import com.recruitment.mapper.AdminPermissionMapper;
import com.recruitment.repository.AdminPermissionRepository;
import com.recruitment.security.PermissionAuthorizationManager;
import com.recruitment.specification.AdminPermissionSpecification;

@Service
public class AdminPermissionService {

	private final AdminPermissionRepository adminPermissionRepository;
	private final AdminPermissionMapper adminPermissionMapper;
	private final MessageService messageService;
	private final PermissionAuthorizationManager permissionAuthorizationManager;

	public AdminPermissionService(
			AdminPermissionRepository adminPermissionRepository,
			AdminPermissionMapper adminPermissionMapper,
			MessageService messageService,
			PermissionAuthorizationManager permissionAuthorizationManager) {
		this.adminPermissionRepository = adminPermissionRepository;
		this.adminPermissionMapper = adminPermissionMapper;
		this.messageService = messageService;
		this.permissionAuthorizationManager = permissionAuthorizationManager;
	}

	public ResponseEntity<ApiResponse<PaginatedResponse<AdminPermissionResponse>>> getList(
			AdminPermissionFilterRequest filter,
			Pageable pageable) {

		Page<AdminPermissionResponse> resultPage = filter.isDeleted()
				? adminPermissionRepository.findAllSoftDeleted(
						filter.normalizedKeyword(),
						filter.normalizedModule(),
						filter.normalizedMethod(),
						filter.type() == null ? null : filter.type().name(),
						filter.active(),
						toNativePageable(pageable))
						.map(permission -> adminPermissionMapper.toResponse(permission))
				: adminPermissionRepository.findAll(AdminPermissionSpecification.build(filter), pageable)
						.map(permission -> adminPermissionMapper.toResponse(permission));

		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("admin-permission.list.success"),
				PaginatedResponse.fromPage(resultPage)));
	}

	public ResponseEntity<ApiResponse<AdminPermissionResponse>> getById(Long id) {
		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("common.success"),
				adminPermissionMapper.toResponse(findActiveById(id))));
	}

	@Transactional
	public ResponseEntity<ApiResponse<AdminPermissionResponse>> create(
			CreateAdminPermissionRequest request,
			String currentUsername) {

		String path = request.path().trim();
		String method = normalizeMethod(request.method());
		validatePathMethodUnique(path, method, null);

		AdminPermission permission = new AdminPermission();
		permission.setName(request.name().trim());
		permission.setDescription(trimToNull(request.description()));
		permission.setModule(request.module().trim());
		permission.setPath(path);
		permission.setMethod(method);
		permission.setType(request.type());
		permission.setActive(request.active() == null || request.active());
		permission.setCreatedBy(currentUsername);
		permission.setUpdatedBy(currentUsername);

		AdminPermission saved = adminPermissionRepository.save(permission);
		permissionAuthorizationManager.loadCache();

		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(
				HttpStatus.CREATED.value(),
				messageService.get("admin-permission.create.success"),
				adminPermissionMapper.toResponse(saved)));
	}

	@Transactional
	public ResponseEntity<ApiResponse<AdminPermissionResponse>> update(
			Long id,
			UpdateAdminPermissionRequest request,
			String currentUsername) {

		AdminPermission permission = findActiveById(id);

		if (StringUtils.hasText(request.name())) {
			permission.setName(request.name().trim());
		}
		if (request.description() != null) {
			permission.setDescription(trimToNull(request.description()));
		}
		if (StringUtils.hasText(request.module())) {
			permission.setModule(request.module().trim());
		}

		String path = StringUtils.hasText(request.path()) ? request.path().trim() : permission.getPath();
		String method = StringUtils.hasText(request.method())
				? normalizeMethod(request.method())
				: permission.getMethod();
		if (StringUtils.hasText(request.path()) || StringUtils.hasText(request.method())) {
			validatePathMethodUnique(path, method, id);
			permission.setPath(path);
			permission.setMethod(method);
		}

		if (request.type() != null) {
			permission.setType(request.type());
		}
		if (request.active() != null) {
			permission.setActive(request.active());
		}
		permission.setUpdatedBy(currentUsername);

		AdminPermission saved = adminPermissionRepository.save(permission);
		permissionAuthorizationManager.loadCache();

		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("admin-permission.update.success"),
				adminPermissionMapper.toResponse(saved)));
	}

	@Transactional
	public ResponseEntity<ApiResponse<Void>> softDelete(Long id, String currentUsername) {
		AdminPermission permission = findActiveById(id);
		permission.setDeletedBy(currentUsername);
		adminPermissionRepository.save(permission);
		adminPermissionRepository.delete(permission);
		permissionAuthorizationManager.loadCache();

		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("admin-permission.soft-delete.success"),
				null));
	}

	@Transactional
	public ResponseEntity<ApiResponse<Void>> restore(Long id, String currentUsername) {
		int restored = adminPermissionRepository.restoreById(id);
		if (restored == 0) {
			throw new ResourceNotFoundException("error.resource.permission", "id", id);
		}

		AdminPermission permission = findActiveById(id);
		permission.setUpdatedBy(currentUsername);
		adminPermissionRepository.save(permission);
		permissionAuthorizationManager.loadCache();

		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("admin-permission.restore.success"),
				null));
	}

	@Transactional
	public ResponseEntity<ApiResponse<Void>> permanentDelete(Long id) {
		adminPermissionRepository.findSoftDeletedById(id)
				.orElseThrow(() -> new BusinessRuleException("error.admin-permission.not-soft-deleted", id));

		adminPermissionRepository.deleteRoleLinksByPermissionIds(List.of(id));
		adminPermissionRepository.hardDeleteById(id);
		permissionAuthorizationManager.loadCache();

		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("admin-permission.permanent-delete.success"),
				null));
	}

	private AdminPermission findActiveById(Long id) {
		return adminPermissionRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("error.resource.permission", "id", id));
	}

	private void validatePathMethodUnique(String path, String method, Long excludeId) {
		boolean exists = excludeId == null
				? adminPermissionRepository.existsByPathAndMethod(path, method)
				: adminPermissionRepository.existsByPathAndMethodAndIdNot(path, method, excludeId);
		if (exists) {
			throw new DuplicateResourceException("error.admin-permission.path-method.duplicate", method, path);
		}
	}

	private String normalizeMethod(String method) {
		return method.trim().toUpperCase();
	}

	private String trimToNull(String value) {
		if (!StringUtils.hasText(value)) {
			return null;
		}
		return value.trim();
	}

	private Pageable toNativePageable(Pageable pageable) {
		return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
	}
}
