package com.recruitment.dto.response.common;

import java.util.List;

import org.springframework.data.domain.Page;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "data", "pagination" })
public record PaginatedResponse<T>(
		List<T> data,
		PaginationMeta pagination) {

	public static <T> PaginatedResponse<T> fromPage(Page<T> page) {
		return new PaginatedResponse<>(page.getContent(), PaginationMeta.fromPage(page));
	}
}
