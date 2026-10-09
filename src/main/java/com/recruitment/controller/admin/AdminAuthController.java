package com.recruitment.controller.admin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.recruitment.dto.request.admin.auth.LoginRequest;
import com.recruitment.dto.request.admin.auth.RefreshRequest;
import com.recruitment.dto.response.admin.auth.LoginResponse;
import com.recruitment.dto.response.common.ApiResponse;
import com.recruitment.service.AdminAuthService;
import com.recruitment.util.constant.Constants;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping(Constants.ADMIN_AUTH_BASE_PATH)
@Tag(name = "Admin Auth")
public class AdminAuthController {

	private final AdminAuthService adminAuthService;

	public AdminAuthController(AdminAuthService adminAuthService) {
		this.adminAuthService = adminAuthService;
	}

	@PostMapping("/login")
	public ResponseEntity<ApiResponse<LoginResponse>> login(
			@Valid
			@RequestBody
			LoginRequest request,
			HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) {
		return adminAuthService.login(request, httpRequest, httpResponse);
	}

	@PostMapping("/refresh")
	public ResponseEntity<ApiResponse<LoginResponse>> refresh(
			@RequestBody(required = false)
			RefreshRequest body,
			HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) {
		return adminAuthService.refresh(body, httpRequest, httpResponse);
	}

	@PostMapping("/logout")
	public ResponseEntity<ApiResponse<Void>> logout(
			HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) {
		return adminAuthService.logout(httpRequest, httpResponse);
	}
}
