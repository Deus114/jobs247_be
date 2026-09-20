package com.recruitment.dto.response.admin.educationlevel;

import java.time.Instant;

public record EducationLevelResponse(
		Long id,
		String nameVi,
		String nameEn,
		int sortOrder,
		boolean active,
		Instant createdAt,
		Instant updatedAt) {
}
