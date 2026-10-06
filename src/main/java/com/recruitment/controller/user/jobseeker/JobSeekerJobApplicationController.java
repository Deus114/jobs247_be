package com.recruitment.controller.user.jobseeker;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.recruitment.config.OpenAPIConfig;
import com.recruitment.dto.request.user.application.CreateJobApplicationRequest;
import com.recruitment.dto.response.common.ApiResponse;
import com.recruitment.dto.response.user.application.JobApplicationResponse;
import com.recruitment.service.JobApplicationService;
import com.recruitment.util.constant.Constants;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping(Constants.JOB_SEEKER_JOB_APPLICATIONS_BASE_PATH)
@Tag(name = "Job Seeker Application")
@SecurityRequirement(name = OpenAPIConfig.BEARER_AUTH_SCHEME)
public class JobSeekerJobApplicationController {

	private final JobApplicationService jobApplicationService;

	public JobSeekerJobApplicationController(JobApplicationService jobApplicationService) {
		this.jobApplicationService = jobApplicationService;
	}

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@io.swagger.v3.oas.annotations.parameters.RequestBody(
			content = @Content(
					mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
					schema = @Schema(implementation = CreateJobApplicationRequest.class)))
	public ResponseEntity<ApiResponse<JobApplicationResponse>> apply(
			@PathVariable
			Long jobId,
			@Valid
			@ModelAttribute
			CreateJobApplicationRequest request,
			@Parameter(hidden = true)
			@AuthenticationPrincipal
			Jwt jwt) {
		return jobApplicationService.apply(jobId, request, jwt);
	}
}
