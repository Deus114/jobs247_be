package com.recruitment.controller.user;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.recruitment.config.OpenAPIConfig;
import com.recruitment.dto.request.user.auth.RegisterRequest;
import com.recruitment.dto.request.user.auth.SendOtpRequest;
import com.recruitment.dto.request.user.auth.UpdateUserMeRequest;
import com.recruitment.dto.request.user.auth.UserLoginRequest;
import com.recruitment.dto.request.user.auth.UserRefreshRequest;
import com.recruitment.dto.request.user.auth.VerifyOtpRequest;
import com.recruitment.dto.response.common.ApiResponse;
import com.recruitment.dto.response.user.auth.RegisterResponse;
import com.recruitment.dto.response.user.auth.UserLoginResponse;
import com.recruitment.dto.response.user.auth.UserSessionResponse;
import com.recruitment.dto.response.user.auth.VerifyOtpResponse;
import com.recruitment.service.UserAuthService;
import com.recruitment.util.constant.Constants;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping(Constants.AUTH_BASE_PATH)
@Tag(name = "User Auth")
public class UserAuthController {

	private final UserAuthService userAuthService;

	public UserAuthController(UserAuthService userAuthService) {
		this.userAuthService = userAuthService;
	}

	@PostMapping("/otp/send")
	public ResponseEntity<ApiResponse<Void>> sendOtp(
			@Valid
			@RequestBody
			SendOtpRequest request) {
		return userAuthService.sendOtp(request);
	}

	@PostMapping("/otp/verify")
	public ResponseEntity<ApiResponse<VerifyOtpResponse>> verifyOtp(
			@Valid
			@RequestBody
			VerifyOtpRequest request) {
		return userAuthService.verifyOtp(request);
	}

	@PostMapping("/register")
	public ResponseEntity<ApiResponse<RegisterResponse>> register(
			@Valid
			@RequestBody
			RegisterRequest request) {
		return userAuthService.register(request);
	}

	@PostMapping("/login")
	public ResponseEntity<ApiResponse<UserLoginResponse>> login(
			@Valid
			@RequestBody
			UserLoginRequest request,
			HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) {
		return userAuthService.login(request, httpRequest, httpResponse);
	}

	@PostMapping("/refresh")
	public ResponseEntity<ApiResponse<UserLoginResponse>> refresh(
			@RequestBody(required = false)
			UserRefreshRequest body,
			HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) {
		return userAuthService.refresh(body, httpRequest, httpResponse);
	}

	@GetMapping("/me")
	@SecurityRequirement(name = OpenAPIConfig.BEARER_AUTH_SCHEME)
	public ResponseEntity<ApiResponse<UserSessionResponse>> getMe(
			@AuthenticationPrincipal
			Jwt jwt) {
		return userAuthService.getMe(jwt);
	}

	@PutMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@SecurityRequirement(name = OpenAPIConfig.BEARER_AUTH_SCHEME)
	@io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE, schema = @Schema(implementation = UpdateUserMeRequest.class)))
	public ResponseEntity<ApiResponse<UserSessionResponse>> updateMe(
			@Valid
			@ModelAttribute
			UpdateUserMeRequest request,
			@AuthenticationPrincipal
			Jwt jwt) {
		return userAuthService.updateMe(jwt, request);
	}

	@PostMapping("/logout")
	public ResponseEntity<ApiResponse<Void>> logout(
			HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) {
		return userAuthService.logout(httpRequest, httpResponse);
	}
}
