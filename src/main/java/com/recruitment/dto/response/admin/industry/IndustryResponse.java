package com.recruitment.dto.response.admin.industry;

import java.time.Instant;

import com.recruitment.dto.response.admin.industrygroup.IndustryGroupResponse;

public record IndustryResponse(
		Long id,
		IndustryGroupResponse industryGroup,
		String nameVi,
		String nameEn,
		String descriptionVi,
		String descriptionEn,
		int sortOrder,
		String image,
		boolean active,
		Instant createdAt,
		Instant updatedAt) {
}
