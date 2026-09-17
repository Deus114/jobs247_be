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
import com.recruitment.dto.response.common.IndustryGroupPublicResponse;
import com.recruitment.dto.response.common.PaginatedResponse;
import com.recruitment.service.IndustryGroupService;
import com.recruitment.util.constant.Constants;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping(Constants.INDUSTRY_GROUPS_PUBLIC_PATH)
@Tag(name = "Industry Group")
public class IndustryGroupController {

	private final IndustryGroupService industryGroupService;

	public IndustryGroupController(IndustryGroupService industryGroupService) {
		this.industryGroupService = industryGroupService;
	}

	@GetMapping
	public ResponseEntity<ApiResponse<PaginatedResponse<IndustryGroupPublicResponse>>> getPublicList(
			@ParameterObject
			@PageableDefault(size = 20, sort = "sortOrder", direction = Sort.Direction.ASC)
			Pageable pageable) {
		return industryGroupService.getPublicList(pageable);
	}
}
