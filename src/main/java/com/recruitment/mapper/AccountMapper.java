package com.recruitment.mapper;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import com.recruitment.dto.response.admin.account.AccountCompanyMembershipResponse;
import com.recruitment.dto.response.admin.account.AccountResponse;
import com.recruitment.entity.Company;
import com.recruitment.entity.CompanyMember;
import com.recruitment.entity.User;
import com.recruitment.enums.UserTypeEnum;

@Component
public class AccountMapper {

	public AccountResponse toResponse(User user) {
		return toResponse(user, Collections.emptyList());
	}

	public AccountResponse toResponse(User user, List<CompanyMember> memberships) {
		List<AccountCompanyMembershipResponse> companies = UserTypeEnum.EMPLOYER.name().equals(user.getType())
				? memberships.stream().map(this::toMembershipResponse).toList()
				: List.of();
		return new AccountResponse(
				user.getId(),
				user.getName(),
				user.getEmail(),
				user.getType(),
				user.isEmailVerified(),
				user.getAvatar(),
				user.getPhone(),
				user.isActive(),
				companies,
				user.getCreatedAt(),
				user.getUpdatedAt());
	}

	private AccountCompanyMembershipResponse toMembershipResponse(CompanyMember member) {
		Company company = member.getCompany();
		return new AccountCompanyMembershipResponse(
				company.getId(),
				company.getName(),
				company.getLogo(),
				company.getStatus(),
				member.getRole());
	}
}
