package com.recruitment.dto.request.user.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;

public record EmployerDashboardFilterRequest(
		@Schema(description = "Lọc theo công ty. Bỏ trống để lấy mọi công ty employer đang quản lý") Long companyId) {
}
