package com.recruitment.enums;

import java.util.Locale;
import java.util.Optional;

public enum DevicePlatformEnum {
	WEB,
	ANDROID,
	IOS;

	public static Optional<DevicePlatformEnum> fromValue(String value) {
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
