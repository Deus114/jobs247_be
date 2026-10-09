package com.recruitment.queue;

import java.io.Serializable;

import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
public class FcmPublisher {

	private final FcmEnqueueService fcmEnqueueService;

	public FcmPublisher(FcmEnqueueService fcmEnqueueService) {
		this.fcmEnqueueService = fcmEnqueueService;
	}

	public void publish(Long notificationId) {
		FcmPushMessage message = new FcmPushMessage(notificationId);
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				@Override
				public void afterCommit() {
					fcmEnqueueService.enqueue(message);
				}
			});
			return;
		}
		fcmEnqueueService.enqueue(message);
	}

	public record FcmPushMessage(Long notificationId) implements Serializable {
	}
}

interface FcmEnqueueService {

	void enqueue(FcmPublisher.FcmPushMessage message);
}
