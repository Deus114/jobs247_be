package com.recruitment.util.helper;

import java.time.LocalDate;

/** Key Redis dedup mail việc phù hợp — ví dụ {@code digest:sent:42:2026-10-07}. */
public final class DigestKeys {

	private static final String SENT_PREFIX = "digest:sent:";

	private DigestKeys() {
	}

	public static String sent(long userId, LocalDate date) {
		return SENT_PREFIX + userId + ":" + date;
	}
}
