package com.recruitment.specification;

import jakarta.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;

import com.recruitment.entity.Notification;

public final class NotificationSpecification {

	private NotificationSpecification() {
	}

	public static Specification<Notification> forRecipient(
			String recipientType,
			Long recipientId,
			Boolean unreadOnly) {
		return (root, query, criteriaBuilder) -> {
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(root.get("recipientType"), recipientType),
					criteriaBuilder.equal(root.get("recipientId"), recipientId));
			if (Boolean.TRUE.equals(unreadOnly)) {
				predicate = criteriaBuilder.and(predicate, criteriaBuilder.isNull(root.get("readAt")));
			}
			return predicate;
		};
	}
}
