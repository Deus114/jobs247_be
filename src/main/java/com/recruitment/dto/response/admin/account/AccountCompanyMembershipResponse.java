package com.recruitment.dto.response.admin.account;

public record AccountCompanyMembershipResponse(
		Long companyId,
		String companyName,
		String companyLogo,
		String companyStatus,
		String role) {
}
