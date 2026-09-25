package com.recruitment.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.recruitment.dto.response.admin.industrygroup.IndustryGroupResponse;
import com.recruitment.dto.response.common.IndustryGroupPublicResponse;
import com.recruitment.dto.response.common.IndustryPublicResponse;
import com.recruitment.entity.IndustryGroup;
import com.recruitment.util.helper.LocaleTextHelper;

@Component
public class IndustryGroupMapper {

	private final IndustryMapper industryMapper;

	public IndustryGroupMapper(IndustryMapper industryMapper) {
		this.industryMapper = industryMapper;
	}

	public IndustryGroupResponse toAdminResponse(IndustryGroup group, long industryCount) {
		return new IndustryGroupResponse(
				group.getId(),
				group.getNameVi(),
				group.getNameEn(),
				group.getDescriptionVi(),
				group.getDescriptionEn(),
				group.getSortOrder(),
				group.getImage(),
				group.isActive(),
				industryCount,
				group.getCreatedAt(),
				group.getUpdatedAt());
	}

	public IndustryGroupPublicResponse toPublicResponse(IndustryGroup group) {
		List<IndustryPublicResponse> industries = group.getIndustries() == null
				? List.of()
				: group.getIndustries().stream()
						.filter(industry -> industry != null && industry.isActive())
						.map(industryMapper::toPublicResponse)
						.toList();

		return new IndustryGroupPublicResponse(
				group.getId(),
				LocaleTextHelper.pick(group.getNameVi(), group.getNameEn()),
				LocaleTextHelper.pick(group.getDescriptionVi(), group.getDescriptionEn()),
				group.getSortOrder(),
				group.getImage(),
				industries);
	}
}
