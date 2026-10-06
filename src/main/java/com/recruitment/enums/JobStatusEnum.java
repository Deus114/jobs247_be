package com.recruitment.enums;

import java.util.Locale;
import java.util.Optional;

public enum JobStatusEnum {
	PENDING,
	APPROVED,
	REJECTED;

	public static Optional<JobStatusEnum> fromValue(String value) {
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
