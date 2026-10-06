package com.recruitment.enums;

import java.util.Locale;
import java.util.Optional;

public enum EmploymentTypeEnum {
	FULL_TIME,
	PART_TIME,
	INTERNSHIP,
	CONTRACT,
	FREELANCE;

	public static Optional<EmploymentTypeEnum> fromValue(String value) {
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
