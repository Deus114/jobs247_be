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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.recruitment.config.OpenAPIConfig;
import com.recruitment.dto.request.admin.application.AdminApplicationFilterRequest;
import com.recruitment.dto.response.common.ApiResponse;
import com.recruitment.dto.response.common.PaginatedResponse;
import com.recruitment.dto.response.user.application.JobApplicationResponse;
import com.recruitment.service.AdminApplicationService;
import com.recruitment.util.constant.Constants;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping(Constants.ADMIN_APPLICATIONS_BASE_PATH)
@Tag(name = "Admin Application")
@SecurityRequirement(name = OpenAPIConfig.BEARER_AUTH_SCHEME)
public class AdminApplicationController {

	private final AdminApplicationService adminApplicationService;

	public AdminApplicationController(AdminApplicationService adminApplicationService) {
		this.adminApplicationService = adminApplicationService;
	}

	@GetMapping
	public ResponseEntity<ApiResponse<PaginatedResponse<JobApplicationResponse>>> getList(
			@ParameterObject
			AdminApplicationFilterRequest filter,
			@ParameterObject
			@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
			Pageable pageable) {
		return adminApplicationService.getList(filter, pageable);
	}

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<JobApplicationResponse>> getById(@PathVariable Long id) {
		return adminApplicationService.getById(id);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<Void>> softDelete(
			@PathVariable
			Long id,
			@Parameter(hidden = true)
			@AuthenticationPrincipal
			Jwt jwt) {
		return adminApplicationService.softDelete(id, jwt.getSubject());
	}

	@PatchMapping("/{id}/restore")
	public ResponseEntity<ApiResponse<Void>> restore(
			@PathVariable
			Long id,
			@Parameter(hidden = true)
			@AuthenticationPrincipal
			Jwt jwt) {
		return adminApplicationService.restore(id, jwt.getSubject());
	}

	@DeleteMapping("/{id}/permanent")
	public ResponseEntity<ApiResponse<Void>> permanentDelete(@PathVariable Long id) {
		return adminApplicationService.permanentDelete(id);
	}
}
