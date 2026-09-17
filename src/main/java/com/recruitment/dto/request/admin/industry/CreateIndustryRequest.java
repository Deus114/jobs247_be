package com.recruitment.dto.request.admin.industry;

import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateIndustryRequest(
		@NotNull(message = "{validation.industry.group-id.required}")
		Long industryGroupId,

		@NotBlank(message = "{validation.industry.name-vi.required}")
		@Size(max = 255, message = "{validation.industry.name.max-length}")
		String nameVi,

		@NotBlank(message = "{validation.industry.name-en.required}")
		@Size(max = 255, message = "{validation.industry.name.max-length}")
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
