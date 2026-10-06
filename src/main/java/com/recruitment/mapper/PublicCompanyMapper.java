package com.recruitment.mapper;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Component;

import com.recruitment.dto.response.common.PublicCompanyIndustryResponse;
import com.recruitment.dto.response.common.PublicCompanyResponse;
import com.recruitment.entity.Company;
import com.recruitment.entity.Industry;
import com.recruitment.util.helper.LocaleTextHelper;

@Component
public class PublicCompanyMapper {

	public PublicCompanyResponse toResponse(Company company) {
		List<PublicCompanyIndustryResponse> industries = company.getIndustries() == null
				? List.of()
				: company.getIndustries().stream()
						.sorted(Comparator.comparing(
								industry -> LocaleTextHelper.pick(industry.getNameVi(), industry.getNameEn()),
								String.CASE_INSENSITIVE_ORDER))
						.map(this::toIndustryResponse)
						.toList();
		return new PublicCompanyResponse(
				company.getId(),
				company.getName(),
				company.getSize(),
				company.getProvince().getId(),
				company.getProvince().getName(),
				industries,
				company.getAddress(),
				company.getWebsite(),
				company.getLogo(),
				company.getBackgroundImage(),
				company.getDescription(),
				company.getCreatedAt(),
				company.getUpdatedAt());
	}

	private PublicCompanyIndustryResponse toIndustryResponse(Industry industry) {
		return new PublicCompanyIndustryResponse(
				industry.getId(),
				LocaleTextHelper.pick(industry.getNameVi(), industry.getNameEn()));
	}
}
