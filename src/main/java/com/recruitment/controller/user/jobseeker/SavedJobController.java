package com.recruitment.controller.user.jobseeker;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.recruitment.config.OpenAPIConfig;
import com.recruitment.dto.response.common.ApiResponse;
import com.recruitment.dto.response.common.PaginatedResponse;
import com.recruitment.dto.response.user.application.SavedJobResponse;
import com.recruitment.service.SavedJobService;
import com.recruitment.util.constant.Constants;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping(Constants.JOB_SEEKER_SAVED_JOBS_BASE_PATH)
@Tag(name = "Job Seeker Saved Job")
@SecurityRequirement(name = OpenAPIConfig.BEARER_AUTH_SCHEME)
public class SavedJobController {

	private final SavedJobService savedJobService;

	public SavedJobController(SavedJobService savedJobService) {
		this.savedJobService = savedJobService;
	}

	@GetMapping
	public ResponseEntity<ApiResponse<PaginatedResponse<SavedJobResponse>>> getList(
			@ParameterObject
			@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
			Pageable pageable,
			@Parameter(hidden = true)
			@AuthenticationPrincipal
			Jwt jwt) {
		return savedJobService.getList(pageable, jwt);
	}

	@PostMapping("/{jobId}")
	public ResponseEntity<ApiResponse<SavedJobResponse>> save(
			@PathVariable
			Long jobId,
			@Parameter(hidden = true)
			@AuthenticationPrincipal
			Jwt jwt) {
		return savedJobService.save(jobId, jwt);
	}

	@DeleteMapping("/{jobId}")
	public ResponseEntity<ApiResponse<Void>> unsave(
			@PathVariable
			Long jobId,
			@Parameter(hidden = true)
			@AuthenticationPrincipal
			Jwt jwt) {
		return savedJobService.unsave(jobId, jwt);
	}
}
