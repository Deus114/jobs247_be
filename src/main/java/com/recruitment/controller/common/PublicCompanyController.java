package com.recruitment.controller.common;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.recruitment.dto.request.common.PublicCompanyFilterRequest;
import com.recruitment.dto.response.common.ApiResponse;
import com.recruitment.dto.response.common.PaginatedResponse;
import com.recruitment.dto.response.common.PublicCompanyResponse;
import com.recruitment.dto.response.common.PublicJobResponse;
import com.recruitment.service.PublicCompanyService;
import com.recruitment.util.constant.Constants;
import com.recruitment.util.helper.OptionalJwtHelper;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping(Constants.PUBLIC_COMPANIES_BASE_PATH)
@Tag(name = "Public Company")
public class PublicCompanyController {

	private final PublicCompanyService publicCompanyService;

	public PublicCompanyController(PublicCompanyService publicCompanyService) {
		this.publicCompanyService = publicCompanyService;
	}

	@GetMapping
	public ResponseEntity<ApiResponse<PaginatedResponse<PublicCompanyResponse>>> getCompanies(
			@ParameterObject
			PublicCompanyFilterRequest filter,
			@ParameterObject
			@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
			Pageable pageable) {
		return publicCompanyService.getCompanies(filter, pageable);
	}

	@GetMapping("/{slug}/{id}")
	public ResponseEntity<ApiResponse<PublicCompanyResponse>> getCompany(
			@PathVariable
			String slug,
			@PathVariable
			Long id) {
		return publicCompanyService.getCompany(id);
	}

	@GetMapping("/{slug}/{id}/jobs")
	public ResponseEntity<ApiResponse<PaginatedResponse<PublicJobResponse>>> getCompanyJobs(
			@PathVariable
			String slug,
			@PathVariable
			Long id,
			@ParameterObject
			@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
			Pageable pageable,
			@Parameter(hidden = true)
			@AuthenticationPrincipal
			Jwt jwt) {
		return publicCompanyService.getCompanyJobs(id, pageable, OptionalJwtHelper.userId(jwt));
	}
}
