package com.recruitment.dto.response.common;

public record IndustryPublicResponse(
		Long id,
		String name,
		String description,
		int sortOrder,
		String image) {
}
