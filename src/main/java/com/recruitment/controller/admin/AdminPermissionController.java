package com.recruitment.controller.admin;

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
import com.recruitment.dto.request.admin.permission.AdminPermissionFilterRequest;
import com.recruitment.dto.request.admin.permission.CreateAdminPermissionRequest;
import com.recruitment.dto.request.admin.permission.UpdateAdminPermissionRequest;
import com.recruitment.dto.response.admin.permission.AdminPermissionResponse;
import com.recruitment.dto.response.common.ApiResponse;
import com.recruitment.dto.response.common.PaginatedResponse;
import com.recruitment.service.AdminPermissionService;
import com.recruitment.util.constant.Constants;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping(Constants.ADMIN_PERMISSIONS_BASE_PATH)
@Tag(name = "Admin Permission")
@SecurityRequirement(name = OpenAPIConfig.BEARER_AUTH_SCHEME)
public class AdminPermissionController {

	private final AdminPermissionService adminPermissionService;

	public AdminPermissionController(AdminPermissionService adminPermissionService) {
		this.adminPermissionService = adminPermissionService;
	}

	@GetMapping
	public ResponseEntity<ApiResponse<PaginatedResponse<AdminPermissionResponse>>> getList(
			@ParameterObject
			AdminPermissionFilterRequest filter,
			@ParameterObject
			@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
			Pageable pageable) {
		return adminPermissionService.getList(filter, pageable);
	}

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<AdminPermissionResponse>> getById(
			@PathVariable
			Long id) {
		return adminPermissionService.getById(id);
	}

	@PostMapping
	public ResponseEntity<ApiResponse<AdminPermissionResponse>> create(
			@Valid
			@RequestBody
			CreateAdminPermissionRequest request,
			@Parameter(hidden = true)
			@AuthenticationPrincipal
			Jwt jwt) {
		return adminPermissionService.create(request, jwt.getSubject());
	}

	@PutMapping("/{id}")
	public ResponseEntity<ApiResponse<AdminPermissionResponse>> update(
			@PathVariable
			Long id,
			@Valid
			@RequestBody
			UpdateAdminPermissionRequest request,
			@Parameter(hidden = true)
			@AuthenticationPrincipal
			Jwt jwt) {
		return adminPermissionService.update(id, request, jwt.getSubject());
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<Void>> softDelete(
			@PathVariable
			Long id,
			@Parameter(hidden = true)
			@AuthenticationPrincipal
			Jwt jwt) {
		return adminPermissionService.softDelete(id, jwt.getSubject());
	}

	@PatchMapping("/{id}/restore")
	public ResponseEntity<ApiResponse<Void>> restore(
			@PathVariable
			Long id,
			@Parameter(hidden = true)
			@AuthenticationPrincipal
			Jwt jwt) {
		return adminPermissionService.restore(id, jwt.getSubject());
	}

	@DeleteMapping("/{id}/permanent")
	public ResponseEntity<ApiResponse<Void>> permanentDelete(
			@PathVariable
			Long id) {
		return adminPermissionService.permanentDelete(id);
	}
}
