package com.recruitment.mapper;

import org.springframework.stereotype.Component;

import com.recruitment.dto.response.admin.province.ProvinceResponse;
import com.recruitment.dto.response.common.ProvincePublicResponse;
import com.recruitment.entity.Province;

@Component
public class ProvinceMapper {

	public ProvinceResponse toAdminResponse(Province province) {
		return new ProvinceResponse(
				province.getId(),
				province.getName(),
				province.getRegion(),
				province.getSortOrder(),
				province.isActive(),
				province.getCreatedAt(),
				province.getUpdatedAt());
	}

	public ProvincePublicResponse toPublicResponse(Province province) {
		return new ProvincePublicResponse(
				province.getId(),
				province.getName(),
				province.getRegion(),
				province.getSortOrder());
	}
}
