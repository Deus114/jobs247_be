package com.recruitment.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.recruitment.mail.EmailPublisher.EmailMessage;

@Service
@ConditionalOnProperty(name = "app.mail.queue-enabled", havingValue = "false", matchIfMissing = true)
public class AsyncEmailEnqueueService implements EmailEnqueueService {

	private static final Logger log = LoggerFactory.getLogger(AsyncEmailEnqueueService.class);

	private final MailService mailService;

	public AsyncEmailEnqueueService(MailService mailService) {
		this.mailService = mailService;
	}

	@Override
	@Async
	public void enqueue(EmailMessage message) {
		log.debug("Enqueue email via @Async to {}", message.to());
		mailService.send(message);
	}
}
