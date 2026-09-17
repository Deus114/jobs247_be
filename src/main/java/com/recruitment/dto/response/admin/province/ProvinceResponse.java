package com.recruitment.dto.response.admin.province;

import java.time.Instant;

public record ProvinceResponse(
		Long id,
		String name,
		String region,
		int sortOrder,
		boolean active,
		Instant createdAt,
		Instant updatedAt) {
}
