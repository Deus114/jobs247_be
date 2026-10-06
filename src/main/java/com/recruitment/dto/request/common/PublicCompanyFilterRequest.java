package com.recruitment.dto.request.common;

public record PublicCompanyFilterRequest(
		String keyword,
		Long provinceId,
		Long industryId,
		String size) {
}
