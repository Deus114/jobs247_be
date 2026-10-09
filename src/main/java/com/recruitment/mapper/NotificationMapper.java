package com.recruitment.mapper;

import org.springframework.stereotype.Component;

import com.recruitment.dto.response.common.NotificationResponse;
import com.recruitment.entity.Notification;
import com.recruitment.notification.NotificationContentService;

@Component
public class NotificationMapper {

	private final NotificationContentService notificationContentService;

	public NotificationMapper(NotificationContentService notificationContentService) {
		this.notificationContentService = notificationContentService;
	}

	public NotificationResponse toResponse(Notification notification) {
		return new NotificationResponse(
				notification.getId(),
				notification.getType(),
				notificationContentService.title(notification),
				notificationContentService.body(notification),
				notification.getRelatedId(),
				notification.getReadAt(),
				notification.getReadAt() != null,
				notification.getCreatedAt());
	}
}
