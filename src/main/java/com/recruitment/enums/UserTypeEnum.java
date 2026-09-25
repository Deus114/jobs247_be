package com.recruitment.enums;

import java.util.Locale;
import java.util.Optional;

public enum UserTypeEnum {
	JOB_SEEKER,
	EMPLOYER;

	public static Optional<UserTypeEnum> fromValue(String value) {
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
