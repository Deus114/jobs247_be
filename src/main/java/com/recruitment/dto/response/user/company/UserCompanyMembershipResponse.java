package com.recruitment.dto.response.user.company;

public record UserCompanyMembershipResponse(
		Long companyId,
		String companyName,
		String companyLogo,
		String companyStatus,
		String role) {
}
