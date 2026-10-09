package com.recruitment.notification;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.Notification;
import com.recruitment.entity.AdminUser;
import com.recruitment.entity.DeviceToken;
import com.recruitment.entity.User;
import com.recruitment.enums.NotificationRecipientTypeEnum;
import com.recruitment.queue.FcmPublisher.FcmPushMessage;
import com.recruitment.repository.AdminUserRepository;
import com.recruitment.repository.DeviceTokenRepository;
import com.recruitment.repository.NotificationRepository;
import com.recruitment.repository.UserRepository;
import com.recruitment.service.DeviceTokenService;

@Service
@ConditionalOnProperty(name = "app.fcm.enabled", havingValue = "true")
public class FcmSendService {

	private static final Logger log = LoggerFactory.getLogger(FcmSendService.class);

	private final FirebaseMessaging firebaseMessaging;
	private final NotificationRepository notificationRepository;
	private final DeviceTokenRepository deviceTokenRepository;
	private final UserRepository userRepository;
	private final AdminUserRepository adminUserRepository;
	private final DeviceTokenService deviceTokenService;
	private final NotificationContentService notificationContentService;

	public FcmSendService(
			FirebaseMessaging firebaseMessaging,
			NotificationRepository notificationRepository,
			DeviceTokenRepository deviceTokenRepository,
			UserRepository userRepository,
			AdminUserRepository adminUserRepository,
			DeviceTokenService deviceTokenService,
			NotificationContentService notificationContentService) {
		this.firebaseMessaging = firebaseMessaging;
		this.notificationRepository = notificationRepository;
		this.deviceTokenRepository = deviceTokenRepository;
		this.userRepository = userRepository;
		this.adminUserRepository = adminUserRepository;
		this.deviceTokenService = deviceTokenService;
		this.notificationContentService = notificationContentService;
	}

	public void send(FcmPushMessage message) {
		com.recruitment.entity.Notification notification = notificationRepository.findById(message.notificationId())
				.orElse(null);
		if (notification == null || !isPushEnabled(notification.getRecipientType(), notification.getRecipientId())) {
			return;
		}
		var tokens = deviceTokenRepository.findByOwnerTypeAndOwnerId(
				notification.getRecipientType(),
				notification.getRecipientId());
		boolean retry = false;
		for (DeviceToken deviceToken : tokens) {
			try {
				firebaseMessaging.send(toMessage(notification, deviceToken.getToken()));
			} catch (FirebaseMessagingException ex) {
				if (isStaleToken(ex)) {
					log.info("Remove stale FCM token for notification {}", notification.getId());
					deviceTokenService.removeInvalidToken(deviceToken.getToken());
					continue;
				}
				log.warn("FCM send failed for notification {}: {}", notification.getId(), ex.getMessagingErrorCode());
				retry = true;
			}
		}
		if (retry) {
			throw new IllegalStateException("FCM send failed for notification " + notification.getId());
		}
	}

	private boolean isPushEnabled(String recipientType, Long recipientId) {
		if (NotificationRecipientTypeEnum.ADMIN.name().equals(recipientType)) {
			AdminUser admin = adminUserRepository.findById(recipientId).orElse(null);
			return admin != null && admin.isActive() && admin.isPushEnabled();
		}
		User user = userRepository.findById(recipientId).orElse(null);
		return user != null && user.isActive() && user.isPushEnabled();
	}

	private Message toMessage(com.recruitment.entity.Notification notification, String token) {
		Message.Builder builder = Message.builder()
				.setToken(token)
				.setNotification(Notification.builder()
						.setTitle(notificationContentService.titleForPush(notification))
						.setBody(notificationContentService.bodyForPush(notification))
						.build())
				.putData("notificationId", String.valueOf(notification.getId()))
				.putData("type", notification.getType())
				.putData("relatedId", notification.getRelatedId() == null ? "" : String.valueOf(notification.getRelatedId()));
		return builder.build();
	}

	private boolean isStaleToken(FirebaseMessagingException ex) {
		MessagingErrorCode code = ex.getMessagingErrorCode();
		if (code == MessagingErrorCode.UNREGISTERED || code == MessagingErrorCode.SENDER_ID_MISMATCH) {
			return true;
		}
		String text = ex.getMessage();
		return code == MessagingErrorCode.INVALID_ARGUMENT
				&& text != null
				&& text.toLowerCase(Locale.ROOT).contains("registration token");
	}
}
