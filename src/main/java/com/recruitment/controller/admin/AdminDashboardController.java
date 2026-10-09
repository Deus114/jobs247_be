package com.recruitment.controller.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.recruitment.config.OpenAPIConfig;
import com.recruitment.dto.response.admin.dashboard.AdminDashboardResponse;
import com.recruitment.dto.response.common.ApiResponse;
import com.recruitment.service.AdminDashboardService;
import com.recruitment.util.constant.Constants;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping(Constants.ADMIN_DASHBOARD_PATH)
@Tag(name = "Admin Dashboard")
@SecurityRequirement(name = OpenAPIConfig.BEARER_AUTH_SCHEME)
public class AdminDashboardController {

	private final AdminDashboardService adminDashboardService;

	public AdminDashboardController(AdminDashboardService adminDashboardService) {
		this.adminDashboardService = adminDashboardService;
	}

	@GetMapping
	public ResponseEntity<ApiResponse<AdminDashboardResponse>> get() {
		return adminDashboardService.get();
	}
}
