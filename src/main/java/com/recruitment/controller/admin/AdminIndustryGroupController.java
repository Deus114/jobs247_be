package com.recruitment.controller.admin;

import jakarta.validation.Valid;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.recruitment.config.OpenAPIConfig;
import com.recruitment.dto.request.admin.industrygroup.CreateIndustryGroupRequest;
import com.recruitment.dto.request.admin.industrygroup.IndustryGroupFilterRequest;
import com.recruitment.dto.request.admin.industrygroup.UpdateIndustryGroupRequest;
import com.recruitment.dto.response.admin.industrygroup.IndustryGroupResponse;
import com.recruitment.dto.response.common.ApiResponse;
import com.recruitment.dto.response.common.PaginatedResponse;
import com.recruitment.service.IndustryGroupService;
import com.recruitment.util.constant.Constants;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping(Constants.ADMIN_INDUSTRY_GROUPS_BASE_PATH)
@Tag(name = "Admin Industry Group")
@SecurityRequirement(name = OpenAPIConfig.BEARER_AUTH_SCHEME)
public class AdminIndustryGroupController {

	private final IndustryGroupService industryGroupService;

	public AdminIndustryGroupController(IndustryGroupService industryGroupService) {
		this.industryGroupService = industryGroupService;
	}

	@GetMapping
	public ResponseEntity<ApiResponse<PaginatedResponse<IndustryGroupResponse>>> getList(
			@ParameterObject
			IndustryGroupFilterRequest filter,
			@ParameterObject
			@PageableDefault(size = 10, sort = "sortOrder", direction = Sort.Direction.ASC)
			Pageable pageable) {
		return industryGroupService.getList(filter, pageable);
	}

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<IndustryGroupResponse>> getById(@PathVariable Long id) {
		return industryGroupService.getById(id);
	}

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@io.swagger.v3.oas.annotations.parameters.RequestBody(
			content = @Content(
					mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
					schema = @Schema(implementation = CreateIndustryGroupRequest.class)))
	public ResponseEntity<ApiResponse<IndustryGroupResponse>> create(
			@Valid
			@ModelAttribute
			CreateIndustryGroupRequest request,
			@Parameter(hidden = true)
			@AuthenticationPrincipal
			Jwt jwt) {
		return industryGroupService.create(request, jwt.getSubject());
	}

	@PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@io.swagger.v3.oas.annotations.parameters.RequestBody(
			content = @Content(
					mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
					schema = @Schema(implementation = UpdateIndustryGroupRequest.class)))
	public ResponseEntity<ApiResponse<IndustryGroupResponse>> update(
			@PathVariable
			Long id,
			@Valid
			@ModelAttribute
			UpdateIndustryGroupRequest request,
			@Parameter(hidden = true)
			@AuthenticationPrincipal
			Jwt jwt) {
		return industryGroupService.update(id, request, jwt.getSubject());
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<Void>> softDelete(
			@PathVariable
			Long id,
			@Parameter(hidden = true)
			@AuthenticationPrincipal
			Jwt jwt) {
		return industryGroupService.softDelete(id, jwt.getSubject());
	}

	@PatchMapping("/{id}/restore")
	public ResponseEntity<ApiResponse<Void>> restore(
			@PathVariable
			Long id,
			@Parameter(hidden = true)
			@AuthenticationPrincipal
			Jwt jwt) {
		return industryGroupService.restore(id, jwt.getSubject());
	}

	@DeleteMapping("/{id}/permanent")
	public ResponseEntity<ApiResponse<Void>> permanentDelete(@PathVariable Long id) {
		return industryGroupService.permanentDelete(id);
	}
}
