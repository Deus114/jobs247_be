package com.recruitment.dto.response.common;

import java.util.List;

public record IndustryGroupPublicResponse(
		Long id,
		String name,
		String description,
		int sortOrder,
		String image,
		List<IndustryPublicResponse> industries) {
}
