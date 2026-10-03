package com.recruitment.dto.request.admin.account;

import jakarta.validation.constraints.NotNull;

public record UpdateAccountStatusRequest(
		@NotNull(message = "{validation.account.active.required}")
		Boolean active) {
}
