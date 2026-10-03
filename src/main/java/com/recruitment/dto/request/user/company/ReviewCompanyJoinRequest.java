package com.recruitment.dto.request.user.company;

import jakarta.validation.constraints.NotBlank;

public record ReviewCompanyJoinRequest(
		@NotBlank(message = "{validation.company.join-status.required}")
		String status) {
}
