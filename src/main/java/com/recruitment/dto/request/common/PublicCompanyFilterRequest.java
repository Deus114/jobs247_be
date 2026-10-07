package com.recruitment.dto.request.common;

import io.swagger.v3.oas.annotations.media.Schema;

public record PublicCompanyFilterRequest(
		String keyword,
		Long provinceId,
		Long industryId,
		@Schema(description = "Quy mô công ty — SIZE_1_10, SIZE_11_50, SIZE_51_200, SIZE_201_500, SIZE_501_1000, SIZE_1000_PLUS. Không dùng tên size (trùng page size).") String companySize) {
}
