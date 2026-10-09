package com.recruitment.enums;

import java.util.Locale;
import java.util.Optional;

/** Loại email transactional. Payload queue lưu {@link #name()}. */
public enum EmailType {
	OTP_REGISTER,
	APPLICATION_SUBMITTED,
	APPLICATION_RECEIVED,
	USER_REGISTERED;

	public String templateName() {
		return name().toLowerCase(Locale.ROOT).replace('_', '-');
	}

	public static Optional<EmailType> fromValue(String value) {
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
