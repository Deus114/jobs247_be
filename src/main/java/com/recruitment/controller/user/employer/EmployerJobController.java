package com.recruitment.controller.user.employer;

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
import com.recruitment.dto.request.user.job.CreateEmployerJobRequest;
import com.recruitment.dto.request.user.job.EmployerJobFilterRequest;
import com.recruitment.dto.request.user.job.UpdateEmployerJobRequest;
import com.recruitment.dto.response.common.ApiResponse;
import com.recruitment.dto.response.common.PaginatedResponse;
import com.recruitment.dto.response.user.job.EmployerJobResponse;
import com.recruitment.service.EmployerJobService;
import com.recruitment.util.constant.Constants;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping(Constants.EMPLOYER_JOBS_BASE_PATH)
@Tag(name = "Employer Job")
@SecurityRequirement(name = OpenAPIConfig.BEARER_AUTH_SCHEME)
public class EmployerJobController {

	private final EmployerJobService employerJobService;

	public EmployerJobController(EmployerJobService employerJobService) {
		this.employerJobService = employerJobService;
	}

	@GetMapping
	public ResponseEntity<ApiResponse<PaginatedResponse<EmployerJobResponse>>> getList(
			@ParameterObject
			EmployerJobFilterRequest filter,
			@ParameterObject
			@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
			Pageable pageable,
			@Parameter(hidden = true)
			@AuthenticationPrincipal
			Jwt jwt) {
		return employerJobService.getList(filter, pageable, jwt);
	}

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<EmployerJobResponse>> getById(
			@PathVariable
			Long id,
			@Parameter(hidden = true)
			@AuthenticationPrincipal
			Jwt jwt) {
		return employerJobService.getById(id, jwt);
	}

	@PostMapping
	public ResponseEntity<ApiResponse<EmployerJobResponse>> create(
			@Valid
			@RequestBody
			CreateEmployerJobRequest request,
			@Parameter(hidden = true)
			@AuthenticationPrincipal
			Jwt jwt) {
		return employerJobService.create(request, jwt);
	}

	@PutMapping("/{id}")
	public ResponseEntity<ApiResponse<EmployerJobResponse>> update(
			@PathVariable
			Long id,
			@Valid
			@RequestBody
			UpdateEmployerJobRequest request,
			@Parameter(hidden = true)
			@AuthenticationPrincipal
			Jwt jwt) {
		return employerJobService.update(id, request, jwt);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<Void>> softDelete(
			@PathVariable
			Long id,
			@Parameter(hidden = true)
			@AuthenticationPrincipal
			Jwt jwt) {
		return employerJobService.softDelete(id, jwt);
	}

	@PatchMapping("/{id}/restore")
	public ResponseEntity<ApiResponse<Void>> restore(
			@PathVariable
			Long id,
			@Parameter(hidden = true)
			@AuthenticationPrincipal
			Jwt jwt) {
		return employerJobService.restore(id, jwt);
	}
}
