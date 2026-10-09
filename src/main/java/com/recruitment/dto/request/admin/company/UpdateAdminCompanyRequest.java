package com.recruitment.dto.request.admin.company;

import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;

public record UpdateAdminCompanyRequest(
		@Schema(description = "Ẩn/hiện công ty")
		Boolean active,

		@Schema(description = "Trạng thái phê duyệt: PENDING, APPROVED, REJECTED")
		String status,

		@Schema(description = "Lý do từ chối (tùy chọn, lưu khi status=REJECTED)")
		@Size(max = 1000, message = "{validation.rejection-reason.max-length}")
		String rejectionReason) {
}
