package com.recruitment.queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.recruitment.notification.FcmSendService;
import com.recruitment.queue.FcmPublisher.FcmPushMessage;

@Service
@ConditionalOnExpression("'${app.fcm.enabled:false}' == 'true' && '${app.mail.queue-enabled:false}' == 'false'")
public class AsyncFcmEnqueueService implements FcmEnqueueService {

	private static final Logger log = LoggerFactory.getLogger(AsyncFcmEnqueueService.class);

	private final FcmSendService fcmSendService;

	public AsyncFcmEnqueueService(FcmSendService fcmSendService) {
		this.fcmSendService = fcmSendService;
	}

	@Override
	@Async
	public void enqueue(FcmPushMessage message) {
		log.debug("Enqueue FCM via @Async for notification {}", message.notificationId());
		fcmSendService.send(message);
	}
}
