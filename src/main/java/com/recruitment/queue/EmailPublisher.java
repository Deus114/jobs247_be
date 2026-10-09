package com.recruitment.queue;

import java.io.Serializable;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
public class EmailPublisher {

	private final EmailEnqueueService emailEnqueueService;

	public EmailPublisher(EmailEnqueueService emailEnqueueService) {
		this.emailEnqueueService = emailEnqueueService;
	}

	public void publish(String type, String to, String locale, Map<String, String> variables) {
		EmailMessage message = new EmailMessage(type, to, locale, variables);
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				@Override
				public void afterCommit() {
					emailEnqueueService.enqueue(message);
				}
			});
			return;
		}
		emailEnqueueService.enqueue(message);
	}

	public record EmailMessage(
			String type,
			String to,
			String locale,
			Map<String, String> variables) implements Serializable {
	}
}

interface EmailEnqueueService {

	void enqueue(EmailPublisher.EmailMessage message);
}
