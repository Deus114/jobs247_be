package com.recruitment.dto.request.user.company;

import io.swagger.v3.oas.annotations.media.Schema;

public record CompanyJoinRequestFilterRequest(
		@Schema(description = "PENDING, APPROVED, REJECTED")
		String status) {
}
