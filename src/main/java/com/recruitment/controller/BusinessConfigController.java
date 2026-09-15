package com.recruitment.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.recruitment.dto.response.common.ApiResponse;
import com.recruitment.dto.response.common.BusinessConfigPublicResponse;
import com.recruitment.service.BusinessConfigService;
import com.recruitment.util.constant.Constants;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping(Constants.BUSINESS_CONFIG_PUBLIC_PATH)
@Tag(name = "Business Config")
public class BusinessConfigController {

	private final BusinessConfigService businessConfigService;

	public BusinessConfigController(BusinessConfigService businessConfigService) {
		this.businessConfigService = businessConfigService;
	}

	@GetMapping
	public ResponseEntity<ApiResponse<BusinessConfigPublicResponse>> getPublic() {
		return businessConfigService.getPublic();
	}
}
