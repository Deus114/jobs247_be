package com.recruitment.queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.recruitment.config.RabbitMqConfig;
import com.recruitment.mail.MailService;
import com.recruitment.queue.EmailPublisher.EmailMessage;

@Service
@ConditionalOnProperty(name = "app.mail.queue-enabled", havingValue = "true")
public class RabbitEmailEnqueueService implements EmailEnqueueService {

	private static final Logger log = LoggerFactory.getLogger(RabbitEmailEnqueueService.class);

	private final RabbitTemplate rabbitTemplate;

	public RabbitEmailEnqueueService(RabbitTemplate rabbitTemplate) {
		this.rabbitTemplate = rabbitTemplate;
	}

	@Override
	public void enqueue(EmailMessage message) {
		log.debug("Enqueue email via RabbitMQ to {}", message.to());
		rabbitTemplate.convertAndSend(
				RabbitMqConfig.EMAIL_EXCHANGE,
				RabbitMqConfig.EMAIL_ROUTING_KEY,
				message);
	}
}

@Component
@ConditionalOnProperty(name = "app.mail.queue-enabled", havingValue = "true")
class EmailSendListener {

	private static final Logger log = LoggerFactory.getLogger(EmailSendListener.class);

	private final MailService mailService;

	EmailSendListener(MailService mailService) {
		this.mailService = mailService;
	}

	@RabbitListener(queues = RabbitMqConfig.EMAIL_QUEUE)
	public void onMessage(EmailMessage message) {
		log.debug("Consume email job {} for {}", message.type(), message.to());
		mailService.send(message);
	}
}
