package com.recruitment.controller.common;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.recruitment.dto.response.common.ApiResponse;
import com.recruitment.dto.response.common.PaginatedResponse;
import com.recruitment.dto.response.common.ProvincePublicResponse;
import com.recruitment.service.ProvinceService;
import com.recruitment.util.constant.Constants;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping(Constants.PROVINCES_PUBLIC_PATH)
@Tag(name = "Province")
public class ProvinceController {

	private final ProvinceService provinceService;

	public ProvinceController(ProvinceService provinceService) {
		this.provinceService = provinceService;
	}

	@GetMapping
	public ResponseEntity<ApiResponse<PaginatedResponse<ProvincePublicResponse>>> getPublicList(
			@ParameterObject
			@PageableDefault(size = 20, sort = "sortOrder", direction = Sort.Direction.ASC)
			Pageable pageable) {
		return provinceService.getPublicList(pageable);
	}
}
