package com.recruitment.controller.admin;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.recruitment.config.OpenAPIConfig;
import com.recruitment.dto.request.admin.province.CreateProvinceRequest;
import com.recruitment.dto.request.admin.province.ProvinceFilterRequest;
import com.recruitment.dto.request.admin.province.UpdateProvinceRequest;
import com.recruitment.dto.response.admin.province.ProvinceResponse;
import com.recruitment.dto.response.common.ApiResponse;
import com.recruitment.dto.response.common.PaginatedResponse;
import com.recruitment.service.ProvinceService;
import com.recruitment.util.constant.Constants;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping(Constants.ADMIN_PROVINCES_BASE_PATH)
@Tag(name = "Admin Province")
@SecurityRequirement(name = OpenAPIConfig.BEARER_AUTH_SCHEME)
public class AdminProvinceController {

	private final ProvinceService provinceService;

	public AdminProvinceController(ProvinceService provinceService) {
		this.provinceService = provinceService;
	}

	@GetMapping
	public ResponseEntity<ApiResponse<PaginatedResponse<ProvinceResponse>>> getList(
			@ParameterObject
			ProvinceFilterRequest filter,
			@ParameterObject
			@PageableDefault(size = 10, sort = "sortOrder", direction = Sort.Direction.ASC)
			Pageable pageable) {
		return provinceService.getList(filter, pageable);
	}

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<ProvinceResponse>> getById(@PathVariable Long id) {
		return provinceService.getById(id);
	}

	@PostMapping
	public ResponseEntity<ApiResponse<ProvinceResponse>> create(
			@Valid
			@RequestBody
			CreateProvinceRequest request,
			@Parameter(hidden = true)
			@AuthenticationPrincipal
			Jwt jwt) {
		return provinceService.create(request, jwt.getSubject());
	}

	@PutMapping("/{id}")
	public ResponseEntity<ApiResponse<ProvinceResponse>> update(
			@PathVariable
			Long id,
			@Valid
			@RequestBody
			UpdateProvinceRequest request,
			@Parameter(hidden = true)
			@AuthenticationPrincipal
			Jwt jwt) {
		return provinceService.update(id, request, jwt.getSubject());
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<Void>> softDelete(
			@PathVariable
			Long id,
			@Parameter(hidden = true)
			@AuthenticationPrincipal
			Jwt jwt) {
		return provinceService.softDelete(id, jwt.getSubject());
	}

	@PatchMapping("/{id}/restore")
	public ResponseEntity<ApiResponse<Void>> restore(
			@PathVariable
			Long id,
			@Parameter(hidden = true)
			@AuthenticationPrincipal
			Jwt jwt) {
		return provinceService.restore(id, jwt.getSubject());
	}

	@DeleteMapping("/{id}/permanent")
	public ResponseEntity<ApiResponse<Void>> permanentDelete(@PathVariable Long id) {
		return provinceService.permanentDelete(id);
	}
}
