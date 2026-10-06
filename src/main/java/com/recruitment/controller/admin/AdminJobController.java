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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.recruitment.config.OpenAPIConfig;
import com.recruitment.dto.request.admin.job.AdminJobFilterRequest;
import com.recruitment.dto.request.admin.job.UpdateAdminJobRequest;
import com.recruitment.dto.response.admin.job.AdminJobResponse;
import com.recruitment.dto.response.common.ApiResponse;
import com.recruitment.dto.response.common.PaginatedResponse;
import com.recruitment.service.AdminJobService;
import com.recruitment.util.constant.Constants;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping(Constants.ADMIN_JOBS_BASE_PATH)
@Tag(name = "Admin Job")
@SecurityRequirement(name = OpenAPIConfig.BEARER_AUTH_SCHEME)
public class AdminJobController {

	private final AdminJobService adminJobService;

	public AdminJobController(AdminJobService adminJobService) {
		this.adminJobService = adminJobService;
	}

	@GetMapping
	public ResponseEntity<ApiResponse<PaginatedResponse<AdminJobResponse>>> getList(
			@ParameterObject
			AdminJobFilterRequest filter,
			@ParameterObject
			@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
			Pageable pageable) {
		return adminJobService.getList(filter, pageable);
	}

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<AdminJobResponse>> getById(@PathVariable Long id) {
		return adminJobService.getById(id);
	}

	@PutMapping("/{id}")
	public ResponseEntity<ApiResponse<AdminJobResponse>> update(
			@PathVariable
			Long id,
			@Valid
			@RequestBody
			UpdateAdminJobRequest request,
			@Parameter(hidden = true)
			@AuthenticationPrincipal
			Jwt jwt) {
		return adminJobService.update(id, request, jwt.getSubject());
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<Void>> softDelete(
			@PathVariable
			Long id,
			@Parameter(hidden = true)
			@AuthenticationPrincipal
			Jwt jwt) {
		return adminJobService.softDelete(id, jwt.getSubject());
	}

	@PatchMapping("/{id}/restore")
	public ResponseEntity<ApiResponse<Void>> restore(
			@PathVariable
			Long id,
			@Parameter(hidden = true)
			@AuthenticationPrincipal
			Jwt jwt) {
		return adminJobService.restore(id, jwt.getSubject());
	}
}
