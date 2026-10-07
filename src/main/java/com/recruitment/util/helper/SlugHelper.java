package com.recruitment.util.helper;

import org.springframework.util.StringUtils;

public final class SlugHelper {

	private static final int MAX_LENGTH = 200;

	private SlugHelper() {
	}

	public static String from(String input) {
		if (!StringUtils.hasText(input)) {
			return "item";
		}
		String unaccented = AccentHelper.unaccent(input.trim());
		StringBuilder slug = new StringBuilder(unaccented.length());
		boolean lastWasHyphen = false;
		for (int i = 0; i < unaccented.length(); i++) {
			char ch = unaccented.charAt(i);
			if ((ch >= 'a' && ch <= 'z') || (ch >= '0' && ch <= '9')) {
				slug.append(ch);
				lastWasHyphen = false;
			} else if (!lastWasHyphen && slug.length() > 0) {
				slug.append('-');
				lastWasHyphen = true;
			}
		}
		while (!slug.isEmpty() && slug.charAt(slug.length() - 1) == '-') {
			slug.setLength(slug.length() - 1);
		}
		if (slug.isEmpty()) {
			return "item";
		}
		if (slug.length() > MAX_LENGTH) {
			slug.setLength(MAX_LENGTH);
			while (!slug.isEmpty() && slug.charAt(slug.length() - 1) == '-') {
				slug.setLength(slug.length() - 1);
			}
		}
		return slug.isEmpty() ? "item" : slug.toString();
	}

	public static String resolve(String storedSlug, String fallbackSource) {
		if (StringUtils.hasText(storedSlug)) {
			return storedSlug.trim();
		}
		return from(fallbackSource);
	}
}
