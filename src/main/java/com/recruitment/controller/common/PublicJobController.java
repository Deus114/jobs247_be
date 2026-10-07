package com.recruitment.controller.common;

import java.util.List;

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

import com.recruitment.dto.request.common.PublicJobFilterRequest;
import com.recruitment.dto.response.common.ApiResponse;
import com.recruitment.dto.response.common.PaginatedResponse;
import com.recruitment.dto.response.common.PublicJobResponse;
import com.recruitment.service.PublicJobService;
import com.recruitment.util.constant.Constants;
import com.recruitment.util.helper.OptionalJwtHelper;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping(Constants.PUBLIC_JOBS_BASE_PATH)
@Tag(name = "Public Job")
public class PublicJobController {

	private final PublicJobService publicJobService;

	public PublicJobController(PublicJobService publicJobService) {
		this.publicJobService = publicJobService;
	}

	@GetMapping
	public ResponseEntity<ApiResponse<PaginatedResponse<PublicJobResponse>>> getJobs(
			@ParameterObject
			PublicJobFilterRequest filter,
			@ParameterObject
			@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
			Pageable pageable,
			@Parameter(hidden = true)
			@AuthenticationPrincipal
			Jwt jwt) {
		return publicJobService.getJobs(filter, pageable, OptionalJwtHelper.userId(jwt));
	}

	@GetMapping("/{slug}/{id}")
	public ResponseEntity<ApiResponse<PublicJobResponse>> getJob(
			@PathVariable
			String slug,
			@PathVariable
			Long id,
			@Parameter(hidden = true)
			@AuthenticationPrincipal
			Jwt jwt) {
		return publicJobService.getJob(id, OptionalJwtHelper.userId(jwt));
	}

	@GetMapping("/{slug}/{id}/related")
	public ResponseEntity<ApiResponse<List<PublicJobResponse>>> getRelatedJobs(
			@PathVariable
			String slug,
			@PathVariable
			Long id,
			@Parameter(hidden = true)
			@AuthenticationPrincipal
			Jwt jwt) {
		return publicJobService.getRelatedJobs(id, OptionalJwtHelper.userId(jwt));
	}
}
