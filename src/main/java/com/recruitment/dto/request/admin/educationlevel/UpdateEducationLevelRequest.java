package com.recruitment.dto.request.admin.educationlevel;

import jakarta.validation.constraints.Size;

public record UpdateEducationLevelRequest(
		@Size(max = 255, message = "{validation.education-level.name.max-length}")
		String nameVi,

		@Size(max = 255, message = "{validation.education-level.name.max-length}")
		String nameEn,

		Integer sortOrder,

		Boolean active) {
}
