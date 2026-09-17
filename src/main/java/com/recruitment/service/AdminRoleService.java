package com.recruitment.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.recruitment.dto.request.admin.role.AdminRoleFilterRequest;
import com.recruitment.dto.request.admin.role.CreateAdminRoleRequest;
import com.recruitment.dto.request.admin.role.UpdateAdminRoleRequest;
import com.recruitment.dto.response.admin.role.AdminRoleResponse;
import com.recruitment.dto.response.common.ApiResponse;
import com.recruitment.dto.response.common.PaginatedResponse;
import com.recruitment.entity.AdminPermission;
import com.recruitment.entity.AdminRole;
import com.recruitment.exception.BusinessRuleException;
import com.recruitment.exception.DuplicateResourceException;
import com.recruitment.exception.ResourceNotFoundException;
import com.recruitment.mapper.AdminRoleMapper;
import com.recruitment.repository.AdminPermissionRepository;
import com.recruitment.repository.AdminRoleRepository;
import com.recruitment.repository.AdminUserRepository;
import com.recruitment.security.PermissionAuthorizationManager;
import com.recruitment.specification.AdminRoleSpecification;
import com.recruitment.util.helper.SoftDeletedPageQuery;

@Service
public class AdminRoleService {

	private final AdminRoleRepository adminRoleRepository;
	private final AdminPermissionRepository adminPermissionRepository;
	private final AdminUserRepository adminUserRepository;
	private final AdminRoleMapper adminRoleMapper;
	private final MessageService messageService;
	private final PermissionAuthorizationManager permissionAuthorizationManager;
	private final SoftDeletedPageQuery softDeletedPageQuery;

	public AdminRoleService(
			AdminRoleRepository adminRoleRepository,
			AdminPermissionRepository adminPermissionRepository,
			AdminUserRepository adminUserRepository,
			AdminRoleMapper adminRoleMapper,
			MessageService messageService,
			PermissionAuthorizationManager permissionAuthorizationManager,
			SoftDeletedPageQuery softDeletedPageQuery) {
		this.adminRoleRepository = adminRoleRepository;
		this.adminPermissionRepository = adminPermissionRepository;
		this.adminUserRepository = adminUserRepository;
		this.adminRoleMapper = adminRoleMapper;
		this.messageService = messageService;
		this.permissionAuthorizationManager = permissionAuthorizationManager;
		this.softDeletedPageQuery = softDeletedPageQuery;
	}

	public ResponseEntity<ApiResponse<PaginatedResponse<AdminRoleResponse>>> getList(
			AdminRoleFilterRequest filter,
			Pageable pageable) {

		Page<AdminRoleResponse> resultPage = filter.isDeleted()
				? softDeletedPageQuery.find(
						AdminRole.class,
						"admin_roles r",
						"r",
						List.of("r.name", "r.description"),
						filter.normalizedKeyword(),
						"AND (:fullAccess IS NULL OR r.full_access = :fullAccess) "
								+ "AND (:active IS NULL OR r.active = :active)",
						SoftDeletedPageQuery.params(
								"fullAccess", filter.fullAccess(),
								"active", filter.active()),
						"r.created_at DESC",
						pageable)
						.map(adminRoleMapper::toResponse)
				: adminRoleRepository.findAll(AdminRoleSpecification.build(filter), pageable)
						.map(adminRoleMapper::toResponse);

		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("admin-role.list.success"),
				PaginatedResponse.fromPage(resultPage)));
	}

	public ResponseEntity<ApiResponse<AdminRoleResponse>> getById(Long id) {
		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("common.success"),
				toRoleResponse(id)));
	}

	@Transactional
	public ResponseEntity<ApiResponse<AdminRoleResponse>> create(
			CreateAdminRoleRequest request,
			String currentUsername) {

		validateNameUnique(request.name(), null);

		AdminRole role = new AdminRole();
		role.setName(request.name().trim());
		role.setDescription(trimToNull(request.description()));
		role.setFullAccess(Boolean.TRUE.equals(request.fullAccess()));
		role.setActive(request.active() == null || request.active());
		role.setCreatedBy(currentUsername);
		role.setUpdatedBy(currentUsername);
		role.setPermissions(resolvePermissions(request.permissionIds()));

		AdminRole saved = adminRoleRepository.save(role);
		permissionAuthorizationManager.loadCache();

		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(
				HttpStatus.CREATED.value(),
				messageService.get("admin-role.create.success"),
				toRoleResponse(saved.getId())));
	}

	@Transactional
	public ResponseEntity<ApiResponse<AdminRoleResponse>> update(
			Long id,
			UpdateAdminRoleRequest request,
			String currentUsername) {

		AdminRole role = findActiveByIdWithPermissions(id);

		if (StringUtils.hasText(request.name())) {
			validateNameUnique(request.name(), id);
			role.setName(request.name().trim());
		}
		if (request.description() != null) {
			role.setDescription(trimToNull(request.description()));
		}
		if (request.fullAccess() != null) {
			role.setFullAccess(request.fullAccess());
		}
		if (request.active() != null) {
			role.setActive(request.active());
		}
		if (request.permissionIds() != null) {
			role.setPermissions(resolvePermissions(request.permissionIds()));
		}
		role.setUpdatedBy(currentUsername);

		AdminRole saved = adminRoleRepository.save(role);
		permissionAuthorizationManager.loadCache();

		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("admin-role.update.success"),
				toRoleResponse(saved.getId())));
	}

	@Transactional
	public ResponseEntity<ApiResponse<Void>> softDelete(Long id, String currentUsername) {
		AdminRole role = findActiveById(id);

		if (adminUserRepository.existsByRoleId(id)) {
			throw new BusinessRuleException("error.admin-role.in-use", role.getName());
		}

		role.setDeletedBy(currentUsername);
		adminRoleRepository.save(role);
		adminRoleRepository.delete(role);
		permissionAuthorizationManager.loadCache();

		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("admin-role.soft-delete.success"),
				null));
	}

	@Transactional
	public ResponseEntity<ApiResponse<Void>> restore(Long id, String currentUsername) {
		int restored = adminRoleRepository.restoreById(id);
		if (restored == 0) {
			throw new ResourceNotFoundException("error.resource.role", "id", id);
		}

		AdminRole role = findActiveById(id);
		role.setUpdatedBy(currentUsername);
		adminRoleRepository.save(role);
		permissionAuthorizationManager.loadCache();

		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("admin-role.restore.success"),
				null));
	}

	@Transactional
	public ResponseEntity<ApiResponse<Void>> permanentDelete(Long id) {
		adminRoleRepository.findSoftDeletedById(id)
				.orElseThrow(() -> new BusinessRuleException("error.admin-role.not-soft-deleted", id));

		if (adminRoleRepository.countUsersByRoleId(id) > 0) {
			throw new BusinessRuleException("error.admin-role.in-use-permanent", id);
		}

		adminRoleRepository.deletePermissionLinksByRoleId(id);
		adminRoleRepository.hardDeleteById(id);
		permissionAuthorizationManager.loadCache();

		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("admin-role.permanent-delete.success"),
				null));
	}

	private AdminRole findActiveById(Long id) {
		return adminRoleRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("error.resource.role", "id", id));
	}

	private AdminRole findActiveByIdWithPermissions(Long id) {
		return adminRoleRepository.findByIdWithPermissions(id)
				.orElseThrow(() -> new ResourceNotFoundException("error.resource.role", "id", id));
	}

	private AdminRoleResponse toRoleResponse(Long id) {
		return adminRoleMapper.toResponse(findActiveByIdWithPermissions(id));
	}

	private void validateNameUnique(String name, Long excludeId) {
		boolean exists = excludeId == null
				? adminRoleRepository.existsByName(name.trim())
				: adminRoleRepository.existsByNameAndIdNot(name.trim(), excludeId);
		if (exists) {
			throw new DuplicateResourceException("error.admin-role.name.duplicate", name.trim());
		}
	}

	private List<AdminPermission> resolvePermissions(List<Long> permissionIds) {
		if (permissionIds == null || permissionIds.isEmpty()) {
			return new ArrayList<>();
		}

		Set<Long> uniqueIds = new LinkedHashSet<>(permissionIds);
		List<AdminPermission> permissions = adminPermissionRepository.findAllById(uniqueIds);
		if (permissions.size() != uniqueIds.size()) {
			Set<Long> foundIds = permissions.stream()
					.map(permission -> permission.getId())
					.collect(Collectors.toSet());
			Long missingId = uniqueIds.stream()
					.filter(permissionId -> !foundIds.contains(permissionId))
					.findFirst()
					.orElseThrow();
			throw new ResourceNotFoundException("error.resource.permission", "id", missingId);
		}
		return new ArrayList<>(permissions);
	}

	private String trimToNull(String value) {
		if (!StringUtils.hasText(value)) {
			return null;
		}
		return value.trim();
	}
}
