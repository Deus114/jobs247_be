package com.recruitment.controller.user;

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
@RequestMapping(Constants.USER_DEVICE_TOKENS_BASE_PATH)
@Tag(name = "User Device Token")
@SecurityRequirement(name = OpenAPIConfig.BEARER_AUTH_SCHEME)
public class UserDeviceTokenController {

	private final DeviceTokenService deviceTokenService;

	public UserDeviceTokenController(DeviceTokenService deviceTokenService) {
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
		return deviceTokenService.registerForUser(request, jwt);
	}

	@DeleteMapping
	public ResponseEntity<ApiResponse<Void>> unregister(
			@Valid
			@RequestBody
			UnregisterDeviceTokenRequest request,
			@Parameter(hidden = true)
			@AuthenticationPrincipal
			Jwt jwt) {
		return deviceTokenService.unregisterForUser(request, jwt);
	}
}
