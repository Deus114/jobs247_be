package com.recruitment.mapper;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import com.recruitment.dto.response.admin.company.AdminCompanyIndustryResponse;
import com.recruitment.dto.response.admin.company.AdminCompanyMemberResponse;
import com.recruitment.dto.response.admin.company.AdminCompanyResponse;
import com.recruitment.entity.Company;
import com.recruitment.entity.CompanyMember;
import com.recruitment.entity.User;
import com.recruitment.util.helper.LocaleTextHelper;

@Component
public class AdminCompanyMapper {

	public AdminCompanyResponse toListResponse(Company company) {
		return toResponse(company, Collections.emptyList());
	}

	public AdminCompanyResponse toDetailResponse(Company company, List<CompanyMember> members) {
		List<AdminCompanyMemberResponse> memberResponses = members.stream()
				.map(this::toMemberResponse)
				.toList();
		return toResponse(company, memberResponses);
	}

	private AdminCompanyResponse toResponse(Company company, List<AdminCompanyMemberResponse> members) {
		Long id = company.getId();
		String name = company.getName();
		String size = company.getSize();
		Long provinceId = company.getProvince().getId();
		String provinceName = company.getProvince().getName();
		List<AdminCompanyIndustryResponse> industries = company.getIndustries().stream()
				.map(industry -> new AdminCompanyIndustryResponse(
						industry.getId(),
						LocaleTextHelper.pick(industry.getNameVi(), industry.getNameEn())))
				.toList();
		String address = company.getAddress();
		String email = company.getEmail();
		String phone = company.getPhone();
		String website = company.getWebsite();
		String logo = company.getLogo();
		String backgroundImage = company.getBackgroundImage();
		String taxCode = company.getTaxCode();
		String description = company.getDescription();
		String status = company.getStatus();
		String rejectionReason = company.getRejectionReason();
		Instant rejectedAt = company.getRejectedAt();
		Instant approvedAt = company.getApprovedAt();
		boolean active = company.isActive();
		Instant createdAt = company.getCreatedAt();
		Instant updatedAt = company.getUpdatedAt();

		return new AdminCompanyResponse(
				id,
				name,
				size,
				provinceId,
				provinceName,
				industries,
				address,
				email,
				phone,
				website,
				logo,
				backgroundImage,
				taxCode,
				description,
				status,
				rejectionReason,
				rejectedAt,
				approvedAt,
				active,
				members,
				createdAt,
				updatedAt);
	}

	private AdminCompanyMemberResponse toMemberResponse(CompanyMember member) {
		User user = member.getUser();
		return new AdminCompanyMemberResponse(
				member.getId(),
				user.getId(),
				user.getName(),
				user.getEmail(),
				user.getPhone(),
				user.getAvatar(),
				member.getRole(),
				member.isActive(),
				member.getCreatedAt(),
				member.getUpdatedAt());
	}
}
