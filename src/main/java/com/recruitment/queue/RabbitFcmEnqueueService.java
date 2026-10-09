package com.recruitment.queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.recruitment.config.RabbitMqConfig;
import com.recruitment.notification.FcmSendService;
import com.recruitment.queue.FcmPublisher.FcmPushMessage;

@Service
@ConditionalOnExpression("'${app.fcm.enabled:false}' == 'true' && '${app.mail.queue-enabled:false}' == 'true'")
public class RabbitFcmEnqueueService implements FcmEnqueueService {

	private static final Logger log = LoggerFactory.getLogger(RabbitFcmEnqueueService.class);

	private final RabbitTemplate rabbitTemplate;

	public RabbitFcmEnqueueService(RabbitTemplate rabbitTemplate) {
		this.rabbitTemplate = rabbitTemplate;
	}

	@Override
	public void enqueue(FcmPushMessage message) {
		log.debug("Enqueue FCM via RabbitMQ for notification {}", message.notificationId());
		rabbitTemplate.convertAndSend(
				RabbitMqConfig.FCM_EXCHANGE,
				RabbitMqConfig.FCM_ROUTING_KEY,
				message);
	}
}

@Component
@ConditionalOnExpression("'${app.fcm.enabled:false}' == 'true' && '${app.mail.queue-enabled:false}' == 'true'")
class FcmPushListener {

	private static final Logger log = LoggerFactory.getLogger(FcmPushListener.class);

	private final FcmSendService fcmSendService;

	FcmPushListener(FcmSendService fcmSendService) {
		this.fcmSendService = fcmSendService;
	}

	@RabbitListener(queues = RabbitMqConfig.FCM_QUEUE)
	public void onMessage(FcmPushMessage message) {
		log.debug("Consume FCM job for notification {}", message.notificationId());
		fcmSendService.send(message);
	}
}
