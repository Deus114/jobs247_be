package com.recruitment.controller.user.employer;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.recruitment.config.OpenAPIConfig;
import com.recruitment.dto.request.user.company.CompanyJoinRequestFilterRequest;
import com.recruitment.dto.request.user.company.ReviewCompanyJoinRequest;
import com.recruitment.dto.response.common.ApiResponse;
import com.recruitment.dto.response.common.PaginatedResponse;
import com.recruitment.dto.response.user.company.EmployerCompanyJoinRequestResponse;
import com.recruitment.service.CompanyService;
import com.recruitment.util.constant.Constants;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping(Constants.EMPLOYER_JOIN_REQUESTS_BASE_PATH)
@Tag(name = "Employer Join Request")
@SecurityRequirement(name = OpenAPIConfig.BEARER_AUTH_SCHEME)
public class EmployerJoinRequestController {

	private final CompanyService companyService;

	public EmployerJoinRequestController(CompanyService companyService) {
		this.companyService = companyService;
	}

	@GetMapping("/mine")
	public ResponseEntity<ApiResponse<PaginatedResponse<EmployerCompanyJoinRequestResponse>>> getMyJoinRequests(
			@ParameterObject
			CompanyJoinRequestFilterRequest filter,
			@ParameterObject
			@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
			Pageable pageable,
			@Parameter(hidden = true)
			@AuthenticationPrincipal
			Jwt jwt) {
		return companyService.getMyJoinRequests(filter, pageable, jwt);
	}

	@GetMapping
	public ResponseEntity<ApiResponse<PaginatedResponse<EmployerCompanyJoinRequestResponse>>> getJoinRequests(
			@ParameterObject
			CompanyJoinRequestFilterRequest filter,
			@ParameterObject
			@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
			Pageable pageable,
			@Parameter(hidden = true)
			@AuthenticationPrincipal
			Jwt jwt) {
		return companyService.getJoinRequests(filter, pageable, jwt);
	}

	@PutMapping("/{id}")
	public ResponseEntity<ApiResponse<EmployerCompanyJoinRequestResponse>> reviewJoinRequest(
			@PathVariable
			Long id,
			@Valid
			@RequestBody
			ReviewCompanyJoinRequest request,
			@Parameter(hidden = true)
			@AuthenticationPrincipal
			Jwt jwt) {
		return companyService.reviewJoinRequest(id, request, jwt);
	}
}
