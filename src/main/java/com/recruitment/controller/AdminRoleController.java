package com.recruitment.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.recruitment.config.OpenAPIConfig;
import com.recruitment.dto.request.admin.role.AdminRoleFilterRequest;
import com.recruitment.dto.request.admin.role.CreateAdminRoleRequest;
import com.recruitment.dto.request.admin.role.UpdateAdminRoleRequest;
import com.recruitment.dto.response.admin.role.AdminRoleResponse;
import com.recruitment.dto.response.common.ApiResponse;
import com.recruitment.dto.response.common.PaginatedResponse;
import com.recruitment.service.AdminRoleService;
import com.recruitment.util.constant.Constants;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping(Constants.ADMIN_ROLES_BASE_PATH)
@Tag(name = "Admin Role")
@SecurityRequirement(name = OpenAPIConfig.BEARER_AUTH_SCHEME)
public class AdminRoleController {

	private final AdminRoleService adminRoleService;

	public AdminRoleController(AdminRoleService adminRoleService) {
		this.adminRoleService = adminRoleService;
	}

	@GetMapping
	public ResponseEntity<ApiResponse<PaginatedResponse<AdminRoleResponse>>> getList(
			@ParameterObject
			AdminRoleFilterRequest filter,
			@ParameterObject
			@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
			Pageable pageable) {
		return adminRoleService.getList(filter, pageable);
	}

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<AdminRoleResponse>> getById(@PathVariable Long id) {
		return adminRoleService.getById(id);
	}

	@PostMapping
	public ResponseEntity<ApiResponse<AdminRoleResponse>> create(
			@Valid
			@RequestBody
			CreateAdminRoleRequest request,
			@Parameter(hidden = true)
			@AuthenticationPrincipal
			Jwt jwt) {
		return adminRoleService.create(request, jwt.getSubject());
	}

	@PutMapping("/{id}")
	public ResponseEntity<ApiResponse<AdminRoleResponse>> update(
			@PathVariable
			Long id,
			@Valid
			@RequestBody
			UpdateAdminRoleRequest request,
			@Parameter(hidden = true)
			@AuthenticationPrincipal
			Jwt jwt) {
		return adminRoleService.update(id, request, jwt.getSubject());
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<Void>> softDelete(
			@PathVariable
			Long id,
			@Parameter(hidden = true)
			@AuthenticationPrincipal
			Jwt jwt) {
		return adminRoleService.softDelete(id, jwt.getSubject());
	}

	@PatchMapping("/{id}/restore")
	public ResponseEntity<ApiResponse<Void>> restore(
			@PathVariable
			Long id,
			@Parameter(hidden = true)
			@AuthenticationPrincipal
			Jwt jwt) {
		return adminRoleService.restore(id, jwt.getSubject());
	}

	@DeleteMapping("/{id}/permanent")
	public ResponseEntity<ApiResponse<Void>> permanentDelete(@PathVariable Long id) {
		return adminRoleService.permanentDelete(id);
	}
}
