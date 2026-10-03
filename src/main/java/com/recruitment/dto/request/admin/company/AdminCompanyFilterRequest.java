package com.recruitment.dto.request.admin.company;

public record AdminCompanyFilterRequest(
		String keyword,
		String status,
		Boolean active,
		Boolean deleted) {

	public boolean isDeleted() {
		return Boolean.TRUE.equals(deleted);
	}
}
