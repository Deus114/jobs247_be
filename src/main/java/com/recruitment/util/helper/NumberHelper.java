package com.recruitment.util.helper;

import org.springframework.util.StringUtils;

public final class NumberHelper {

	private NumberHelper() {
	}

	public static Double parseFlexibleDouble(String raw) {
		if (!StringUtils.hasText(raw)) {
			return null;
		}
		return Double.valueOf(raw.trim().replace(',', '.'));
	}
}
