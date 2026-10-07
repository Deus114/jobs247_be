package com.recruitment.dto.response.common;

import java.time.Instant;
import java.util.List;

public record PublicCompanyResponse(
		Long id,
		String slug,
		String name,
		String size,
		Long provinceId,
		String provinceName,
		List<PublicCompanyIndustryResponse> industries,
		String address,
		String website,
		String logo,
		String backgroundImage,
		String description,
		Instant createdAt,
		Instant updatedAt) {
}
