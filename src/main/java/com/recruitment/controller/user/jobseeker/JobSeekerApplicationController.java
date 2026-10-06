package com.recruitment.controller.user.jobseeker;

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

import com.recruitment.config.OpenAPIConfig;
import com.recruitment.dto.request.user.application.JobSeekerApplicationFilterRequest;
import com.recruitment.dto.response.common.ApiResponse;
import com.recruitment.dto.response.common.PaginatedResponse;
import com.recruitment.dto.response.user.application.JobApplicationResponse;
import com.recruitment.service.JobApplicationService;
import com.recruitment.util.constant.Constants;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping(Constants.JOB_SEEKER_APPLICATIONS_BASE_PATH)
@Tag(name = "Job Seeker Application")
@SecurityRequirement(name = OpenAPIConfig.BEARER_AUTH_SCHEME)
public class JobSeekerApplicationController {

	private final JobApplicationService jobApplicationService;

	public JobSeekerApplicationController(JobApplicationService jobApplicationService) {
		this.jobApplicationService = jobApplicationService;
	}

	@GetMapping
	public ResponseEntity<ApiResponse<PaginatedResponse<JobApplicationResponse>>> getList(
			@ParameterObject
			JobSeekerApplicationFilterRequest filter,
			@ParameterObject
			@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
			Pageable pageable,
			@Parameter(hidden = true)
			@AuthenticationPrincipal
			Jwt jwt) {
		return jobApplicationService.getMyApplications(filter, pageable, jwt);
	}

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<JobApplicationResponse>> getById(
			@PathVariable
			Long id,
			@Parameter(hidden = true)
			@AuthenticationPrincipal
			Jwt jwt) {
		return jobApplicationService.getMyApplication(id, jwt);
	}
}
