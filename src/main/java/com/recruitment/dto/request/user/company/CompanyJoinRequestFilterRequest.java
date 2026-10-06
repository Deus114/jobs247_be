package com.recruitment.dto.request.user.company;

import io.swagger.v3.oas.annotations.media.Schema;

public record CompanyJoinRequestFilterRequest(
		@Schema(description = "Filter theo công ty (owner list)")
		Long companyId,
		@Schema(description = "PENDING, APPROVED, REJECTED")
		String status) {
}
