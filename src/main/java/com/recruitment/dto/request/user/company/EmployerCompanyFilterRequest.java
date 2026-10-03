package com.recruitment.dto.request.user.company;

import io.swagger.v3.oas.annotations.media.Schema;

public record EmployerCompanyFilterRequest(
		String keyword,

		@Schema(description = "true = công ty của employer đang đăng nhập; false = công ty không thuộc employer; bỏ trống = tất cả") Boolean mine,

		String status,

		Boolean active) {
}
