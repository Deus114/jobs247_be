package com.recruitment.dto.request.admin.account;

import com.recruitment.enums.UserTypeEnum;

public record AccountFilterRequest(
		String keyword,
		UserTypeEnum type,
		Boolean active,
		Boolean deleted) {

	public boolean isDeleted() {
		return Boolean.TRUE.equals(deleted);
	}
}
