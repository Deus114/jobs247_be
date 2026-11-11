package com.recruitment.controller.user.jobseeker;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.recruitment.config.OpenAPIConfig;
import com.recruitment.dto.response.common.ApiResponse;
import com.recruitment.dto.response.common.PaginatedResponse;
import com.recruitment.dto.response.common.PublicJobResponse;
import com.recruitment.service.JobViewHistoryService;
import com.recruitment.util.constant.Constants;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping(Constants.JOB_SEEKER_RECOMMENDED_JOBS_BASE_PATH)
@Tag(name = "Job Seeker Recommended Job")
@SecurityRequirement(name = OpenAPIConfig.BEARER_AUTH_SCHEME)
public class JobSeekerRecommendedJobController {

	private final JobViewHistoryService jobViewHistoryService;

	public JobSeekerRecommendedJobController(JobViewHistoryService jobViewHistoryService) {
		this.jobViewHistoryService = jobViewHistoryService;
	}

	@GetMapping
	public ResponseEntity<ApiResponse<PaginatedResponse<PublicJobResponse>>> getRecommended(
			@ParameterObject
			@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
			Pageable pageable,
			@Parameter(hidden = true)
			@AuthenticationPrincipal
			Jwt jwt) {
		return jobViewHistoryService.getRecommended(pageable, jwt);
	}
}
