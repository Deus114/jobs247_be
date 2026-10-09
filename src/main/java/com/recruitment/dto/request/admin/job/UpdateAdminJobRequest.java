package com.recruitment.dto.request.admin.job;

import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;

public record UpdateAdminJobRequest(
		@Schema(description = "Ẩn/hiện tin")
		Boolean active,

		@Schema(description = "PENDING, APPROVED, REJECTED")
		String status,

		@Schema(description = "Lý do từ chối (tùy chọn)")
		@Size(max = 1000, message = "{validation.rejection-reason.max-length}")
		String rejectionReason,

		@Schema(description = "Đánh dấu tin Hot")
		Boolean hot) {
}
