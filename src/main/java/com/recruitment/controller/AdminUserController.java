package com.recruitment.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.recruitment.config.OpenAPIConfig;
import com.recruitment.dto.request.admin.user.AdminUserFilterRequest;
import com.recruitment.dto.request.admin.user.CreateAdminUserRequest;
import com.recruitment.dto.request.admin.user.UpdateAdminUserRequest;
import com.recruitment.dto.request.admin.user.UpdateMeRequest;
import com.recruitment.dto.response.admin.user.AdminUserResponse;
import com.recruitment.dto.response.common.ApiResponse;
import com.recruitment.dto.response.common.PaginatedResponse;
import com.recruitment.service.AdminUserService;
import com.recruitment.util.constant.Constants;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping(Constants.ADMIN_USERS_BASE_PATH)
@Tag(name = "Admin User")
@SecurityRequirement(name = OpenAPIConfig.BEARER_AUTH_SCHEME)
public class AdminUserController {

	private final AdminUserService adminUserService;

	public AdminUserController(AdminUserService adminUserService) {
		this.adminUserService = adminUserService;
	}

	@GetMapping
	public ResponseEntity<ApiResponse<PaginatedResponse<AdminUserResponse>>> getList(
			@ParameterObject
			AdminUserFilterRequest filter,
			@ParameterObject
			@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
			Pageable pageable) {
		return adminUserService.getList(filter, pageable);
	}

	@GetMapping("/me")
	public ResponseEntity<ApiResponse<AdminUserResponse>> getMe(
			@Parameter(hidden = true)
			@AuthenticationPrincipal
			Jwt jwt) {
		return adminUserService.getMe(jwt.getSubject());
	}

	@PutMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@io.swagger.v3.oas.annotations.parameters.RequestBody(
			content = @Content(
					mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
					schema = @Schema(implementation = UpdateMeRequest.class)))
	public ResponseEntity<ApiResponse<AdminUserResponse>> updateMe(
			@Valid
			@ModelAttribute
			UpdateMeRequest request,
			@Parameter(hidden = true)
			@AuthenticationPrincipal
			Jwt jwt) {
		return adminUserService.updateMe(jwt.getSubject(), request);
	}

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<AdminUserResponse>> getById(@PathVariable
	Long id) {
		return adminUserService.getById(id);
	}

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@io.swagger.v3.oas.annotations.parameters.RequestBody(
			content = @Content(
					mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
					schema = @Schema(implementation = CreateAdminUserRequest.class)))
	public ResponseEntity<ApiResponse<AdminUserResponse>> create(
			@Valid
			@ModelAttribute
			CreateAdminUserRequest request,
			@Parameter(hidden = true)
			@AuthenticationPrincipal
			Jwt jwt) {
		return adminUserService.create(request, jwt.getSubject());
	}

	@PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@io.swagger.v3.oas.annotations.parameters.RequestBody(
			content = @Content(
					mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
					schema = @Schema(implementation = UpdateAdminUserRequest.class)))
	public ResponseEntity<ApiResponse<AdminUserResponse>> update(
			@PathVariable
			Long id,
			@Valid
			@ModelAttribute
			UpdateAdminUserRequest request,
			@Parameter(hidden = true)
			@AuthenticationPrincipal
			Jwt jwt) {
		return adminUserService.update(id, request, jwt.getSubject());
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<Void>> softDelete(
			@PathVariable
			Long id,
			@Parameter(hidden = true)
			@AuthenticationPrincipal
			Jwt jwt) {
		return adminUserService.softDelete(id, jwt.getSubject());
	}

	@PatchMapping("/{id}/restore")
	public ResponseEntity<ApiResponse<Void>> restore(
			@PathVariable
			Long id,
			@Parameter(hidden = true)
			@AuthenticationPrincipal
			Jwt jwt) {
		return adminUserService.restore(id, jwt.getSubject());
	}

	@DeleteMapping("/{id}/permanent")
	public ResponseEntity<ApiResponse<Void>> permanentDelete(@PathVariable
	Long id) {
		return adminUserService.permanentDelete(id);
	}
}
