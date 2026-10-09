package com.recruitment.controller.user.employer;

import jakarta.validation.Valid;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.recruitment.config.OpenAPIConfig;
import com.recruitment.dto.request.user.company.CreateCompanyJoinRequest;
import com.recruitment.dto.request.user.company.CreateEmployerCompanyRequest;
import com.recruitment.dto.request.user.company.EmployerCompanyFilterRequest;
import com.recruitment.dto.request.user.company.UpdateEmployerCompanyRequest;
import com.recruitment.dto.response.common.ApiResponse;
import com.recruitment.dto.response.common.PaginatedResponse;
import com.recruitment.dto.response.user.company.EmployerCompanyJoinRequestResponse;
import com.recruitment.dto.response.user.company.EmployerCompanyResponse;
import com.recruitment.service.CompanyService;
import com.recruitment.util.constant.Constants;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping(Constants.EMPLOYER_COMPANIES_BASE_PATH)
@Tag(name = "Employer Company")
@SecurityRequirement(name = OpenAPIConfig.BEARER_AUTH_SCHEME)
public class CompanyController {

	private final CompanyService companyService;

	public CompanyController(CompanyService companyService) {
		this.companyService = companyService;
	}

	@GetMapping
	public ResponseEntity<ApiResponse<PaginatedResponse<EmployerCompanyResponse>>> getList(
			@ParameterObject
			EmployerCompanyFilterRequest filter,
			@ParameterObject
			@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
			Pageable pageable,
			@Parameter(hidden = true)
			@AuthenticationPrincipal
			Jwt jwt) {
		return companyService.getList(filter, pageable, jwt);
	}

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<EmployerCompanyResponse>> getById(
			@PathVariable
			Long id,
			@Parameter(hidden = true)
			@AuthenticationPrincipal
			Jwt jwt) {
		return companyService.getById(id, jwt);
	}

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE, schema = @Schema(implementation = CreateEmployerCompanyRequest.class)))
	public ResponseEntity<ApiResponse<EmployerCompanyResponse>> create(
			@Valid
			@ModelAttribute
			CreateEmployerCompanyRequest request,
			@Parameter(hidden = true)
			@AuthenticationPrincipal
			Jwt jwt) {
		return companyService.create(request, jwt);
	}

	@PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE, schema = @Schema(implementation = UpdateEmployerCompanyRequest.class)))
	public ResponseEntity<ApiResponse<EmployerCompanyResponse>> update(
			@PathVariable
			Long id,
			@Valid
			@ModelAttribute
			UpdateEmployerCompanyRequest request,
			@Parameter(hidden = true)
			@AuthenticationPrincipal
			Jwt jwt) {
		return companyService.update(id, request, jwt);
	}

	@PostMapping("/{id}/join-requests")
	public ResponseEntity<ApiResponse<EmployerCompanyJoinRequestResponse>> createJoinRequest(
			@PathVariable
			Long id,
			@Valid
			@RequestBody(required = false)
			CreateCompanyJoinRequest request,
			@Parameter(hidden = true)
			@AuthenticationPrincipal
			Jwt jwt) {
		CreateCompanyJoinRequest body = request != null ? request : new CreateCompanyJoinRequest(null);
		return companyService.createJoinRequest(id, body, jwt);
	}
}
