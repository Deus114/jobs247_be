package com.recruitment.dto.response.admin.company;

import java.time.Instant;
import java.util.List;

public record AdminCompanyResponse(
		Long id,
		String name,
		String size,
		Long provinceId,
		String provinceName,
		List<AdminCompanyIndustryResponse> industries,
		String address,
		String email,
		String phone,
		String website,
		String logo,
		String backgroundImage,
		String taxCode,
		String description,
		String status,
		String rejectionReason,
		Instant rejectedAt,
		Instant approvedAt,
		boolean active,
		List<AdminCompanyMemberResponse> members,
		Instant createdAt,
		Instant updatedAt) {
}
