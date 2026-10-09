package com.recruitment.controller.admin;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.recruitment.config.OpenAPIConfig;
import com.recruitment.dto.request.common.RegisterDeviceTokenRequest;
import com.recruitment.dto.request.common.UnregisterDeviceTokenRequest;
import com.recruitment.dto.response.common.ApiResponse;
import com.recruitment.service.DeviceTokenService;
import com.recruitment.util.constant.Constants;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping(Constants.ADMIN_DEVICE_TOKENS_BASE_PATH)
@Tag(name = "Admin Device Token")
@SecurityRequirement(name = OpenAPIConfig.BEARER_AUTH_SCHEME)
public class AdminDeviceTokenController {

	private final DeviceTokenService deviceTokenService;

	public AdminDeviceTokenController(DeviceTokenService deviceTokenService) {
		this.deviceTokenService = deviceTokenService;
	}

	@PostMapping
	public ResponseEntity<ApiResponse<Void>> register(
			@Valid
			@RequestBody
			RegisterDeviceTokenRequest request,
			@Parameter(hidden = true)
			@AuthenticationPrincipal
			Jwt jwt) {
		return deviceTokenService.registerForAdmin(request, jwt);
	}

	@DeleteMapping
	public ResponseEntity<ApiResponse<Void>> unregister(
			@Valid
			@RequestBody
			UnregisterDeviceTokenRequest request,
			@Parameter(hidden = true)
			@AuthenticationPrincipal
			Jwt jwt) {
		return deviceTokenService.unregisterForAdmin(request, jwt);
	}
}
