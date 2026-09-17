package com.recruitment.dto.request.admin.province;

import jakarta.validation.constraints.Size;

public record UpdateProvinceRequest(
		@Size(max = 255, message = "{validation.province.name.max-length}")
		String name,

		@Size(max = 20, message = "{validation.province.region.max-length}")
		String region,

		Integer sortOrder,

		Boolean active) {
}
