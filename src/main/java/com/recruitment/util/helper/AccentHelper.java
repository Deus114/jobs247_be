package com.recruitment.util.helper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Bỏ dấu tiếng Việt cho keyword search (input). */
public final class AccentHelper {

	private static final Map<Character, Character> UNACCENT_MAP = buildUnaccentMap();

	/** Cặp ký tự thường → không dấu (sau LOWER), dùng chung Criteria/native REPLACE. */
	static final String[][] LOWERCASE_REPLACEMENTS = buildLowercaseReplacements();

	private AccentHelper() {
	}

	private static Map<Character, Character> buildUnaccentMap() {
		Map<Character, Character> map = new LinkedHashMap<>();
		putAll(map, 'a',
				'à', 'á', 'ả', 'ã', 'ạ',
				'ă', 'ằ', 'ắ', 'ẳ', 'ẵ', 'ặ',
				'â', 'ầ', 'ấ', 'ẩ', 'ẫ', 'ậ',
				'À', 'Á', 'Ả', 'Ã', 'Ạ',
				'Ă', 'Ằ', 'Ắ', 'Ẳ', 'Ẵ', 'Ặ',
				'Â', 'Ầ', 'Ấ', 'Ẩ', 'Ẫ', 'Ậ');
		putAll(map, 'd', 'đ', 'Đ');
		putAll(map, 'e',
				'è', 'é', 'ẻ', 'ẽ', 'ẹ',
				'ê', 'ề', 'ế', 'ể', 'ễ', 'ệ',
				'È', 'É', 'Ẻ', 'Ẽ', 'Ẹ',
				'Ê', 'Ề', 'Ế', 'Ể', 'Ễ', 'Ệ');
		putAll(map, 'i',
				'ì', 'í', 'ỉ', 'ĩ', 'ị',
				'Ì', 'Í', 'Ỉ', 'Ĩ', 'Ị');
		putAll(map, 'o',
				'ò', 'ó', 'ỏ', 'õ', 'ọ',
				'ô', 'ồ', 'ố', 'ổ', 'ỗ', 'ộ',
				'ơ', 'ờ', 'ớ', 'ở', 'ỡ', 'ợ',
				'Ò', 'Ó', 'Ỏ', 'Õ', 'Ọ',
				'Ô', 'Ồ', 'Ố', 'Ổ', 'Ỗ', 'Ộ',
				'Ơ', 'Ờ', 'Ớ', 'Ở', 'Ỡ', 'Ợ');
		putAll(map, 'u',
				'ù', 'ú', 'ủ', 'ũ', 'ụ',
				'ư', 'ừ', 'ứ', 'ử', 'ữ', 'ự',
				'Ù', 'Ú', 'Ủ', 'Ũ', 'Ụ',
				'Ư', 'Ừ', 'Ứ', 'Ử', 'Ữ', 'Ự');
		putAll(map, 'y',
				'ỳ', 'ý', 'ỷ', 'ỹ', 'ỵ',
				'Ỳ', 'Ý', 'Ỷ', 'Ỹ', 'Ỵ');
		return Map.copyOf(map);
	}

	private static void putAll(Map<Character, Character> map, char replacement, char... sources) {
		for (char source : sources) {
			map.put(source, replacement);
		}
	}

	private static String[][] buildLowercaseReplacements() {
		List<String[]> pairs = new ArrayList<>();
		for (Map.Entry<Character, Character> entry : UNACCENT_MAP.entrySet()) {
			if (Character.isLowerCase(entry.getKey())) {
				pairs.add(new String[] {
						String.valueOf(entry.getKey()),
						String.valueOf(entry.getValue())
				});
			}
		}
		return pairs.toArray(String[][]::new);
	}

	/** Bỏ dấu + lowercase. {@code "Hà Nội"} → {@code "ha noi"}. */
	public static String unaccent(String value) {
		if (value == null) {
			return null;
		}
		StringBuilder out = new StringBuilder(value.length());
		for (int i = 0; i < value.length(); i++) {
			char ch = value.charAt(i);
			out.append(UNACCENT_MAP.getOrDefault(ch, ch));
		}
		return out.toString().toLowerCase(Locale.ROOT);
	}

	/** Trim + {@link #unaccent(String)}; blank → null. Filter DTO dùng method này. */
	public static String normalizeForSearch(String input) {
		if (input == null || input.isBlank()) {
			return null;
		}
		return unaccent(input.trim());
	}

	/** Escape {@code \}, {@code %}, {@code _} cho LIKE; null → null. */
	public static String escapeLike(String value) {
		if (value == null) {
			return null;
		}
		return value
				.replace("\\", "\\\\")
				.replace("%", "\\%")
				.replace("_", "\\_");
	}
}
