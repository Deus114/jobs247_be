package com.recruitment.dto.request.user.company;

import jakarta.validation.constraints.Size;

public record CreateCompanyJoinRequest(
		@Size(max = 500, message = "{validation.company.join-message.max-length}")
		String message) {
}
