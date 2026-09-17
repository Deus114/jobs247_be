package com.recruitment.util.helper;

import java.util.Locale;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.util.StringUtils;

public final class LocaleTextHelper {

	private LocaleTextHelper() {
	}

	public static boolean isEnglish() {
		Locale locale = LocaleContextHolder.getLocale();
		return locale != null && "en".equalsIgnoreCase(locale.getLanguage());
	}

	public static String pick(String vi, String en) {
		if (isEnglish()) {
			return StringUtils.hasText(en) ? en : vi;
		}
		return StringUtils.hasText(vi) ? vi : en;
	}
}
