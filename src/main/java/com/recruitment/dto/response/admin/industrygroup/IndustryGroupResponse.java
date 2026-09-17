package com.recruitment.dto.response.admin.industrygroup;

import java.time.Instant;

public record IndustryGroupResponse(
		Long id,
		String nameVi,
		String nameEn,
		String descriptionVi,
		String descriptionEn,
		int sortOrder,
		String image,
		boolean active,
		long industryCount,
		Instant createdAt,
		Instant updatedAt) {
}
