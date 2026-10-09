package com.recruitment.controller.admin;

import jakarta.validation.Valid;

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
import com.recruitment.dto.request.admin.company.AdminCompanyFilterRequest;
import com.recruitment.dto.request.admin.company.UpdateAdminCompanyRequest;
import com.recruitment.dto.response.admin.company.AdminCompanyResponse;
import com.recruitment.dto.response.common.ApiResponse;
import com.recruitment.dto.response.common.PaginatedResponse;
import com.recruitment.service.AdminCompanyService;
import com.recruitment.util.constant.Constants;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping(Constants.ADMIN_COMPANIES_BASE_PATH)
@Tag(name = "Admin Company")
@SecurityRequirement(name = OpenAPIConfig.BEARER_AUTH_SCHEME)
public class AdminCompanyController {

	private final AdminCompanyService adminCompanyService;

	public AdminCompanyController(AdminCompanyService adminCompanyService) {
		this.adminCompanyService = adminCompanyService;
	}

	@GetMapping
	public ResponseEntity<ApiResponse<PaginatedResponse<AdminCompanyResponse>>> getList(
			@ParameterObject
			AdminCompanyFilterRequest filter,
			@ParameterObject
			@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
			Pageable pageable) {
		return adminCompanyService.getList(filter, pageable);
	}

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<AdminCompanyResponse>> getById(@PathVariable Long id) {
		return adminCompanyService.getById(id);
	}

	@PutMapping("/{id}")
	public ResponseEntity<ApiResponse<AdminCompanyResponse>> update(
			@PathVariable
			Long id,
			@Valid
			@RequestBody
			UpdateAdminCompanyRequest request,
			@Parameter(hidden = true)
			@AuthenticationPrincipal
			Jwt jwt) {
		return adminCompanyService.update(id, request, jwt.getSubject());
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<Void>> softDelete(
			@PathVariable
			Long id,
			@Parameter(hidden = true)
			@AuthenticationPrincipal
			Jwt jwt) {
		return adminCompanyService.softDelete(id, jwt.getSubject());
	}

	@PatchMapping("/{id}/restore")
	public ResponseEntity<ApiResponse<Void>> restore(
			@PathVariable
			Long id,
			@Parameter(hidden = true)
			@AuthenticationPrincipal
			Jwt jwt) {
		return adminCompanyService.restore(id, jwt.getSubject());
	}
}
