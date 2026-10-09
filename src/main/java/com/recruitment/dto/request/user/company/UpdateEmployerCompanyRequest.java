package com.recruitment.dto.request.user.company;

import java.util.List;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.media.Schema;

public record UpdateEmployerCompanyRequest(
		@Size(max = 255, message = "{validation.name.max-length}")
		String name,

		List<Long> industryIds,

		String size,

		Long provinceId,

		@Size(max = 500, message = "{validation.address.max-length}")
		String address,

		@Email(message = "{validation.email.invalid}")
		@Size(max = 255, message = "{validation.email.max-length}")
		String email,

		@Size(max = 20, message = "{validation.company.phone.max-length}")
		String phone,

		@Size(max = 255, message = "{validation.company.website.max-length}")
		String website,

		@Size(max = 20, message = "{validation.company.tax-code.max-length}")
		String taxCode,

		@Size(max = 500, message = "{validation.company.description.max-length}")
		String description,

		@Schema(type = "string", format = "binary", description = "Logo công ty (file). Không gửi kèm logo")
		MultipartFile logoFile,

		@Size(max = 1000, message = "{validation.image-url.max-length}")
		@Schema(description = "Logo công ty (link). Không gửi kèm logoFile")
		String logo,

		@Schema(type = "string", format = "binary", description = "Ảnh nền công ty (file). Không gửi kèm backgroundImage")
		MultipartFile backgroundImageFile,

		@Size(max = 1000, message = "{validation.image-url.max-length}")
		@Schema(description = "Ảnh nền công ty (link). Không gửi kèm backgroundImageFile")
		String backgroundImage) {
}
