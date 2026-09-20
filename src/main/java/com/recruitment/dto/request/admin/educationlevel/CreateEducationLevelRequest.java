package com.recruitment.dto.request.admin.educationlevel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateEducationLevelRequest(
		@NotBlank(message = "{validation.education-level.name-vi.required}")
		@Size(max = 255, message = "{validation.education-level.name.max-length}")
		String nameVi,

		@NotBlank(message = "{validation.education-level.name-en.required}")
		@Size(max = 255, message = "{validation.education-level.name.max-length}")
		String nameEn,

		Integer sortOrder,

		Boolean active) {
}
