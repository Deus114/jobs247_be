package com.recruitment.mapper;

import org.springframework.stereotype.Component;

import com.recruitment.dto.response.admin.educationlevel.EducationLevelResponse;
import com.recruitment.dto.response.common.EducationLevelPublicResponse;
import com.recruitment.entity.EducationLevel;
import com.recruitment.util.helper.LocaleTextHelper;

@Component
public class EducationLevelMapper {

	public EducationLevelResponse toAdminResponse(EducationLevel educationLevel) {
		return new EducationLevelResponse(
				educationLevel.getId(),
				educationLevel.getNameVi(),
				educationLevel.getNameEn(),
				educationLevel.getSortOrder(),
				educationLevel.isActive(),
				educationLevel.getCreatedAt(),
				educationLevel.getUpdatedAt());
	}

	public EducationLevelPublicResponse toPublicResponse(EducationLevel educationLevel) {
		return new EducationLevelPublicResponse(
				educationLevel.getId(),
				LocaleTextHelper.pick(educationLevel.getNameVi(), educationLevel.getNameEn()),
				educationLevel.getSortOrder());
	}
}
