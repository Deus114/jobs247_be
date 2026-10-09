package com.recruitment.controller.user.employer;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.recruitment.config.OpenAPIConfig;
import com.recruitment.dto.request.user.dashboard.EmployerDashboardFilterRequest;
import com.recruitment.dto.response.common.ApiResponse;
import com.recruitment.dto.response.user.dashboard.EmployerDashboardResponse;
import com.recruitment.service.EmployerDashboardService;
import com.recruitment.util.constant.Constants;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping(Constants.EMPLOYER_DASHBOARD_PATH)
@Tag(name = "Employer Dashboard")
@SecurityRequirement(name = OpenAPIConfig.BEARER_AUTH_SCHEME)
public class EmployerDashboardController {

	private final EmployerDashboardService employerDashboardService;

	public EmployerDashboardController(EmployerDashboardService employerDashboardService) {
		this.employerDashboardService = employerDashboardService;
	}

	@GetMapping
	public ResponseEntity<ApiResponse<EmployerDashboardResponse>> get(
			@ParameterObject
			EmployerDashboardFilterRequest filter,
			@Parameter(hidden = true)
			@AuthenticationPrincipal
			Jwt jwt) {
		return employerDashboardService.get(filter, jwt);
	}
}
