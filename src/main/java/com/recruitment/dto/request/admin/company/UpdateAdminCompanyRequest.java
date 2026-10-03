package com.recruitment.dto.request.admin.company;

import io.swagger.v3.oas.annotations.media.Schema;

public record UpdateAdminCompanyRequest(
		@Schema(description = "Ẩn/hiện công ty")
		Boolean active,

		@Schema(description = "Trạng thái phê duyệt: PENDING, APPROVED, REJECTED")
		String status) {
}
