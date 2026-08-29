package com.recruitment.dto.response.common;

import org.springframework.data.domain.Page;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "current_page", "last_page", "per_page", "total", "from", "to" })
public record PaginationMeta(
		@JsonProperty("current_page") int currentPage,
		@JsonProperty("last_page") int lastPage,
		@JsonProperty("per_page") int perPage,
		long total,
		int from,
		int to) {

	public static PaginationMeta fromPage(Page<?> page) {
		int currentPage = page.getNumber() + 1;
		int perPage = page.getSize();
		long total = page.getTotalElements();
		int lastPage = page.getTotalPages();
		int from = page.isEmpty() ? 0 : page.getNumber() * perPage + 1;
		int to = page.isEmpty() ? 0 : from + page.getNumberOfElements() - 1;
		return new PaginationMeta(currentPage, lastPage, perPage, total, from, to);
	}
}
