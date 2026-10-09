package com.recruitment.dto.request.common;

import io.swagger.v3.oas.annotations.media.Schema;

public record NotificationFilterRequest(
		@Schema(description = "true = chỉ lấy chưa đọc")
		Boolean unreadOnly) {
}
