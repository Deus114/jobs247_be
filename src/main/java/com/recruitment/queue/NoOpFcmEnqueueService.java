package com.recruitment.queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.recruitment.queue.FcmPublisher.FcmPushMessage;

@Service
@ConditionalOnProperty(name = "app.fcm.enabled", havingValue = "false", matchIfMissing = true)
public class NoOpFcmEnqueueService implements FcmEnqueueService {

	private static final Logger log = LoggerFactory.getLogger(NoOpFcmEnqueueService.class);

	@Override
	public void enqueue(FcmPushMessage message) {
		log.debug("Skip FCM for notification {} because push is disabled", message.notificationId());
	}
}
