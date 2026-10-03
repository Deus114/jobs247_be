package com.recruitment.dto.response.user.company;

import java.time.Instant;
import java.util.List;

public record EmployerCompanyResponse(
		Long id,
		String name,
		String size,
		Long provinceId,
		String provinceName,
		List<EmployerCompanyIndustryResponse> industries,
		String address,
		String email,
		String phone,
		String website,
		String logo,
		String backgroundImage,
		String taxCode,
		String description,
		String status,
		Instant createdAt,
		Instant updatedAt) {
}
