package com.recruitment.dto.request.admin.industrygroup;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.media.Schema;

public record CreateIndustryGroupRequest(
		@NotBlank(message = "{validation.industry-group.name-vi.required}")
		@Size(max = 255, message = "{validation.industry-group.name.max-length}")
		String nameVi,

		@NotBlank(message = "{validation.industry-group.name-en.required}")
		@Size(max = 255, message = "{validation.industry-group.name.max-length}")
		String nameEn,

		String descriptionVi,

		String descriptionEn,

		Integer sortOrder,

		Boolean active,

		@Schema(type = "string", format = "binary")
		MultipartFile imageFile,

		@Size(max = 1000, message = "{validation.industry.image.max-length}")
		@Schema(description = "Không gửi cùng imageFile")
		String image) {
}
