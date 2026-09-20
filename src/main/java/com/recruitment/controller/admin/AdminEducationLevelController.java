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
import com.recruitment.dto.request.admin.educationlevel.CreateEducationLevelRequest;
import com.recruitment.dto.request.admin.educationlevel.EducationLevelFilterRequest;
import com.recruitment.dto.request.admin.educationlevel.UpdateEducationLevelRequest;
import com.recruitment.dto.response.admin.educationlevel.EducationLevelResponse;
import com.recruitment.dto.response.common.ApiResponse;
import com.recruitment.dto.response.common.PaginatedResponse;
import com.recruitment.service.EducationLevelService;
import com.recruitment.util.constant.Constants;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping(Constants.ADMIN_EDUCATION_LEVELS_BASE_PATH)
@Tag(name = "Admin Education Level")
@SecurityRequirement(name = OpenAPIConfig.BEARER_AUTH_SCHEME)
public class AdminEducationLevelController {

	private final EducationLevelService educationLevelService;

	public AdminEducationLevelController(EducationLevelService educationLevelService) {
		this.educationLevelService = educationLevelService;
	}

	@GetMapping
	public ResponseEntity<ApiResponse<PaginatedResponse<EducationLevelResponse>>> getList(
			@ParameterObject
			EducationLevelFilterRequest filter,
			@ParameterObject
			@PageableDefault(size = 10, sort = "sortOrder", direction = Sort.Direction.ASC)
			Pageable pageable) {
		return educationLevelService.getList(filter, pageable);
	}

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<EducationLevelResponse>> getById(@PathVariable Long id) {
		return educationLevelService.getById(id);
	}

	@PostMapping
	public ResponseEntity<ApiResponse<EducationLevelResponse>> create(
			@Valid
			@RequestBody
			CreateEducationLevelRequest request,
			@Parameter(hidden = true)
			@AuthenticationPrincipal
			Jwt jwt) {
		return educationLevelService.create(request, jwt.getSubject());
	}

	@PutMapping("/{id}")
	public ResponseEntity<ApiResponse<EducationLevelResponse>> update(
			@PathVariable
			Long id,
			@Valid
			@RequestBody
			UpdateEducationLevelRequest request,
			@Parameter(hidden = true)
			@AuthenticationPrincipal
			Jwt jwt) {
		return educationLevelService.update(id, request, jwt.getSubject());
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<Void>> softDelete(
			@PathVariable
			Long id,
			@Parameter(hidden = true)
			@AuthenticationPrincipal
			Jwt jwt) {
		return educationLevelService.softDelete(id, jwt.getSubject());
	}

	@PatchMapping("/{id}/restore")
	public ResponseEntity<ApiResponse<Void>> restore(
			@PathVariable
			Long id,
			@Parameter(hidden = true)
			@AuthenticationPrincipal
			Jwt jwt) {
		return educationLevelService.restore(id, jwt.getSubject());
	}

	@DeleteMapping("/{id}/permanent")
	public ResponseEntity<ApiResponse<Void>> permanentDelete(@PathVariable Long id) {
		return educationLevelService.permanentDelete(id);
	}
}
