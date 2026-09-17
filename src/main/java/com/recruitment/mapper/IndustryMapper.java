package com.recruitment.mapper;

import org.springframework.stereotype.Component;

import com.recruitment.dto.response.admin.industry.IndustryResponse;
import com.recruitment.dto.response.admin.industrygroup.IndustryGroupResponse;
import com.recruitment.dto.response.common.IndustryPublicResponse;
import com.recruitment.entity.Industry;
import com.recruitment.entity.IndustryGroup;
import com.recruitment.repository.IndustryRepository;
import com.recruitment.util.helper.LocaleTextHelper;

@Component
public class IndustryMapper {

	private final IndustryRepository industryRepository;

	public IndustryMapper(IndustryRepository industryRepository) {
		this.industryRepository = industryRepository;
	}

	public IndustryResponse toAdminResponse(Industry industry) {
		return new IndustryResponse(
				industry.getId(),
				toGroupResponse(industry.getIndustryGroup()),
				industry.getNameVi(),
				industry.getNameEn(),
				industry.getDescriptionVi(),
				industry.getDescriptionEn(),
				industry.getSortOrder(),
				industry.getImage(),
				industry.isActive(),
				industry.getCreatedAt(),
				industry.getUpdatedAt());
	}

	public IndustryPublicResponse toPublicResponse(Industry industry) {
		return new IndustryPublicResponse(
				industry.getId(),
				LocaleTextHelper.pick(industry.getNameVi(), industry.getNameEn()),
				LocaleTextHelper.pick(industry.getDescriptionVi(), industry.getDescriptionEn()),
				industry.getSortOrder(),
				industry.getImage());
	}

	private IndustryGroupResponse toGroupResponse(IndustryGroup group) {
		if (group == null) {
			return null;
		}
		return new IndustryGroupResponse(
				group.getId(),
				group.getNameVi(),
				group.getNameEn(),
				group.getDescriptionVi(),
				group.getDescriptionEn(),
				group.getSortOrder(),
				group.getImage(),
				group.isActive(),
				industryRepository.countByIndustryGroupId(group.getId()),
				group.getCreatedAt(),
				group.getUpdatedAt());
	}
}
