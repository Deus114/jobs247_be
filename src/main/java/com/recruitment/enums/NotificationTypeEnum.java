package com.recruitment.enums;

import java.util.Locale;
import java.util.Optional;

/**
 * Prefix : ADMIN_ / EMPLOYER_ / JOB_SEEKER_.
 */
public enum NotificationTypeEnum {
	ADMIN_CONTACT_MESSAGE_NEW,
	ADMIN_COMPANY_PENDING,
	ADMIN_JOB_PENDING,

	EMPLOYER_APPLICATION_RECEIVED,
	EMPLOYER_JOB_STATUS_CHANGED,
	EMPLOYER_COMPANY_STATUS_CHANGED,
	EMPLOYER_JOIN_REQUEST_CREATED,
	EMPLOYER_JOIN_REQUEST_UPDATED,

	JOB_SEEKER_APPLICATION_SUBMITTED,
	JOB_SEEKER_APPLICATION_VIEWED;

	public static Optional<NotificationTypeEnum> fromValue(String value) {
		if (value == null || value.isBlank()) {
			return Optional.empty();
		}
		try {
			return Optional.of(valueOf(value.trim().toUpperCase(Locale.ROOT)));
		} catch (IllegalArgumentException ex) {
			return Optional.empty();
		}
	}
}
