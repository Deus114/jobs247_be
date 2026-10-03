package com.recruitment.enums;

import java.util.Locale;
import java.util.Optional;

public enum CompanySizeEnum {
	SIZE_1_10,
	SIZE_11_50,
	SIZE_51_200,
	SIZE_201_500,
	SIZE_501_1000,
	SIZE_1000_PLUS;

	public static Optional<CompanySizeEnum> fromValue(String value) {
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
