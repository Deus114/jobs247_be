package com.recruitment.notification;

import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.recruitment.entity.AdminUser;
import com.recruitment.entity.Notification;
import com.recruitment.entity.User;
import com.recruitment.enums.NotificationRecipientTypeEnum;
import com.recruitment.enums.NotificationTypeEnum;
import com.recruitment.queue.FcmPublisher;
import com.recruitment.repository.NotificationRepository;

@Service
public class NotificationDispatchService {

	private static final int ACTOR_MAX = 100;

	private final NotificationRepository notificationRepository;
	private final NotificationContentService notificationContentService;
	private final FcmPublisher fcmPublisher;

	public NotificationDispatchService(
			NotificationRepository notificationRepository,
			NotificationContentService notificationContentService,
			FcmPublisher fcmPublisher) {
		this.notificationRepository = notificationRepository;
		this.notificationContentService = notificationContentService;
		this.fcmPublisher = fcmPublisher;
	}

	public void notifyUser(
			User user,
			NotificationTypeEnum type,
			String titleKey,
			String bodyKey,
			List<String> bodyArgs,
			Long relatedId,
			String actor) {
		if (user == null || !user.isActive()) {
			return;
		}
		Notification saved = save(
				NotificationRecipientTypeEnum.USER.name(),
				user.getId(),
				type,
				titleKey,
				bodyKey,
				bodyArgs,
				relatedId,
				actor);
		if (user.isPushEnabled()) {
			fcmPublisher.publish(saved.getId());
		}
	}

	public void notifyUsers(
			Collection<User> users,
			NotificationTypeEnum type,
			String titleKey,
			String bodyKey,
			List<String> bodyArgs,
			Long relatedId,
			String actor) {
		if (users == null) {
			return;
		}
		for (User user : users) {
			notifyUser(user, type, titleKey, bodyKey, bodyArgs, relatedId, actor);
		}
	}

	public void notifyAdmin(
			AdminUser admin,
			NotificationTypeEnum type,
			String titleKey,
			String bodyKey,
			List<String> bodyArgs,
			Long relatedId,
			String actor) {
		if (admin == null || !admin.isActive()) {
			return;
		}
		Notification saved = save(
				NotificationRecipientTypeEnum.ADMIN.name(),
				admin.getId(),
				type,
				titleKey,
				bodyKey,
				bodyArgs,
				relatedId,
				actor);
		if (admin.isPushEnabled()) {
			fcmPublisher.publish(saved.getId());
		}
	}

	public void notifyAdmins(
			Collection<AdminUser> admins,
			NotificationTypeEnum type,
			String titleKey,
			String bodyKey,
			List<String> bodyArgs,
			Long relatedId,
			String actor) {
		if (admins == null) {
			return;
		}
		for (AdminUser admin : admins) {
			notifyAdmin(admin, type, titleKey, bodyKey, bodyArgs, relatedId, actor);
		}
	}

	private Notification save(
			String recipientType,
			Long recipientId,
			NotificationTypeEnum type,
			String titleKey,
			String bodyKey,
			List<String> bodyArgs,
			Long relatedId,
			String actor) {
		String actorName = clip(StringUtils.hasText(actor) ? actor.trim() : "system", ACTOR_MAX);
		Notification notification = new Notification();
		notification.setRecipientType(recipientType);
		notification.setRecipientId(recipientId);
		notification.setType(type.name());
		notification.setTitleKey(titleKey);
		notification.setBodyKey(bodyKey);
		notification.setBodyArgs(notificationContentService.encodeArgs(bodyArgs));
		notification.setRelatedId(relatedId);
		notification.setCreatedBy(actorName);
		notification.setUpdatedBy(actorName);
		return notificationRepository.save(notification);
	}

	private static String clip(String value, int max) {
		if (value == null) {
			return "";
		}
		if (value.length() <= max) {
			return value;
		}
		return value.substring(0, max);
	}
}
