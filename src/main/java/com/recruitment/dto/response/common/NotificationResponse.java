package com.recruitment.dto.response.common;

import java.time.Instant;

public record NotificationResponse(
		Long id,
		String type,
		String title,
		String body,
		Long relatedId,
		Instant readAt,
		boolean read,
		Instant createdAt) {
}
