package com.recruitment.dto.request.admin.industry;

import jakarta.validation.constraints.Size;

import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.media.Schema;

public record UpdateIndustryRequest(
		Long industryGroupId,

		@Size(max = 255, message = "{validation.industry.name.max-length}")
		String nameVi,

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
