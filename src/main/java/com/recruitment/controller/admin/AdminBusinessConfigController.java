package com.recruitment.controller.admin;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.recruitment.config.OpenAPIConfig;
import com.recruitment.dto.request.admin.businessconfig.UpdateBusinessConfigRequest;
import com.recruitment.dto.response.admin.businessconfig.BusinessConfigResponse;
import com.recruitment.dto.response.common.ApiResponse;
import com.recruitment.service.BusinessConfigService;
import com.recruitment.util.constant.Constants;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping(Constants.ADMIN_BUSINESS_CONFIG_BASE_PATH)
@Tag(name = "Admin Business Config")
@SecurityRequirement(name = OpenAPIConfig.BEARER_AUTH_SCHEME)
public class AdminBusinessConfigController {

	private final BusinessConfigService businessConfigService;

	public AdminBusinessConfigController(BusinessConfigService businessConfigService) {
		this.businessConfigService = businessConfigService;
	}

	@GetMapping
	public ResponseEntity<ApiResponse<BusinessConfigResponse>> get() {
		return businessConfigService.getAdmin();
	}

	@PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@io.swagger.v3.oas.annotations.parameters.RequestBody(
			content = @Content(
					mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
					schema = @Schema(implementation = UpdateBusinessConfigRequest.class)))
	public ResponseEntity<ApiResponse<BusinessConfigResponse>> update(
			@Valid
			@ModelAttribute
			UpdateBusinessConfigRequest request,
			@Parameter(hidden = true)
			@AuthenticationPrincipal
			Jwt jwt) {
		return businessConfigService.update(request, jwt.getSubject());
	}
}
