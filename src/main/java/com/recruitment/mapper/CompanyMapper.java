package com.recruitment.mapper;

import org.springframework.stereotype.Component;

import com.recruitment.dto.response.user.company.EmployerCompanyIndustryResponse;
import com.recruitment.dto.response.user.company.EmployerCompanyJoinRequestResponse;
import com.recruitment.dto.response.user.company.EmployerCompanyResponse;
import com.recruitment.dto.response.user.company.UserCompanyMembershipResponse;
import com.recruitment.entity.Company;
import com.recruitment.entity.CompanyJoinRequest;
import com.recruitment.entity.CompanyMember;
import com.recruitment.entity.User;
import com.recruitment.util.helper.LocaleTextHelper;

@Component
public class CompanyMapper {

	public EmployerCompanyResponse toResponse(Company company) {
		return new EmployerCompanyResponse(
				company.getId(),
				company.getName(),
				company.getSize(),
				company.getProvince().getId(),
				company.getProvince().getName(),
				company.getIndustries().stream()
						.map(industry -> new EmployerCompanyIndustryResponse(
								industry.getId(),
								LocaleTextHelper.pick(industry.getNameVi(), industry.getNameEn())))
						.toList(),
				company.getAddress(),
				company.getEmail(),
				company.getPhone(),
				company.getWebsite(),
				company.getLogo(),
				company.getBackgroundImage(),
				company.getTaxCode(),
				company.getDescription(),
				company.getStatus(),
				company.getCreatedAt(),
				company.getUpdatedAt());
	}

	public UserCompanyMembershipResponse toUserMembershipResponse(CompanyMember member) {
		Company company = member.getCompany();
		return new UserCompanyMembershipResponse(
				company.getId(),
				company.getName(),
				company.getLogo(),
				company.getStatus(),
				member.getRole());
	}

	public EmployerCompanyJoinRequestResponse toJoinRequestResponse(CompanyJoinRequest request) {
		Company company = request.getCompany();
		User user = request.getUser();
		return new EmployerCompanyJoinRequestResponse(
				request.getId(),
				company.getId(),
				company.getName(),
				company.getLogo(),
				company.getStatus(),
				user.getId(),
				user.getName(),
				user.getEmail(),
				user.getAvatar(),
				request.getMessage(),
				request.getStatus(),
				request.getCreatedAt(),
				request.getUpdatedAt());
	}
}
