package com.recruitment.dto.request.admin.industrygroup;

import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

public record UpdateIndustryGroupRequest(
		@Size(max = 255, message = "{validation.industry-group.name.max-length}")
		String nameVi,

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
