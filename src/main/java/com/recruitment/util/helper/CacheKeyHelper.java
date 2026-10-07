package com.recruitment.util.helper;

import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

public final class CacheKeyHelper {

	private CacheKeyHelper() {
	}

	public static String pageable(Pageable pageable) {
		String sort = pageable.getSort().stream()
				.map(order -> order.getProperty() + "," + order.getDirection().name())
				.collect(Collectors.joining(";"));
		if (!StringUtils.hasText(sort)) {
			sort = "unsorted";
		}
		return "p" + pageable.getPageNumber() + ":s" + pageable.getPageSize() + ":o" + sort;
	}
}
