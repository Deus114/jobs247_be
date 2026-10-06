package com.recruitment.util.helper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.recruitment.entity.EducationLevel;
import com.recruitment.entity.Industry;
import com.recruitment.entity.Province;
import com.recruitment.enums.EmploymentTypeEnum;
import com.recruitment.enums.ExperienceLevelEnum;
import com.recruitment.exception.BusinessRuleException;
import com.recruitment.exception.ResourceNotFoundException;
import com.recruitment.repository.EducationLevelRepository;
import com.recruitment.repository.IndustryRepository;
import com.recruitment.repository.ProvinceRepository;

@Component
public class JobCatalogHelper {

	private final IndustryRepository industryRepository;
	private final EducationLevelRepository educationLevelRepository;
	private final ProvinceRepository provinceRepository;

	public JobCatalogHelper(
			IndustryRepository industryRepository,
			EducationLevelRepository educationLevelRepository,
			ProvinceRepository provinceRepository) {
		this.industryRepository = industryRepository;
		this.educationLevelRepository = educationLevelRepository;
		this.provinceRepository = provinceRepository;
	}

	public List<Industry> requireActiveIndustries(List<Long> industryIds) {
		List<Long> distinctIds = industryIds.stream().distinct().toList();
		Map<Long, Industry> byId = new LinkedHashMap<>();
		for (Industry industry : industryRepository.findAllById(distinctIds)) {
			byId.put(industry.getId(), industry);
		}
		List<Industry> ordered = new ArrayList<>();
		for (Long id : distinctIds) {
			Industry industry = byId.get(id);
			if (industry == null) {
				throw new ResourceNotFoundException("error.resource.industry", "id", id);
			}
			if (!industry.isActive()) {
				throw new BusinessRuleException("error.company.industry.inactive", id);
			}
			ordered.add(industry);
		}
		return ordered;
	}

	public List<EducationLevel> requireActiveEducationLevels(List<Long> educationLevelIds) {
		if (educationLevelIds == null || educationLevelIds.isEmpty()) {
			return new ArrayList<>();
		}
		List<Long> distinctIds = educationLevelIds.stream().distinct().toList();
		Map<Long, EducationLevel> byId = new LinkedHashMap<>();
		for (EducationLevel level : educationLevelRepository.findAllById(distinctIds)) {
			byId.put(level.getId(), level);
		}
		List<EducationLevel> ordered = new ArrayList<>();
		for (Long id : distinctIds) {
			EducationLevel level = byId.get(id);
			if (level == null) {
				throw new ResourceNotFoundException("error.resource.education-level", "id", id);
			}
			if (!level.isActive()) {
				throw new BusinessRuleException("error.job.education-level.inactive");
			}
			ordered.add(level);
		}
		return ordered;
	}

	public List<Province> requireActiveProvinces(List<Long> provinceIds) {
		List<Long> distinctIds = provinceIds.stream().distinct().toList();
		Map<Long, Province> byId = new LinkedHashMap<>();
		for (Province province : provinceRepository.findAllById(distinctIds)) {
			byId.put(province.getId(), province);
		}
		List<Province> ordered = new ArrayList<>();
		for (Long id : distinctIds) {
			Province province = byId.get(id);
			if (province == null) {
				throw new ResourceNotFoundException("error.resource.province", "id", id);
			}
			if (!province.isActive()) {
				throw new BusinessRuleException("error.company.province.inactive");
			}
			ordered.add(province);
		}
		return ordered;
	}

	public List<String> requireEmploymentTypes(List<String> values) {
		List<String> resolved = new ArrayList<>();
		for (String value : values) {
			if (!StringUtils.hasText(value)) {
				continue;
			}
			String normalized = value.trim().toUpperCase();
			if (resolved.contains(normalized)) {
				continue;
			}
			EmploymentTypeEnum.fromValue(normalized)
					.orElseThrow(() -> new BusinessRuleException(
							"error.job.employment-type.invalid", value));
			resolved.add(normalized);
		}
		if (resolved.isEmpty()) {
			throw new BusinessRuleException("error.job.employment-types.required");
		}
		return resolved;
	}

	public List<String> resolveExperienceLevels(List<String> values) {
		if (values == null || values.isEmpty()) {
			return new ArrayList<>();
		}
		List<String> resolved = new ArrayList<>();
		for (String value : values) {
			if (!StringUtils.hasText(value)) {
				continue;
			}
			String normalized = value.trim().toUpperCase();
			if (resolved.contains(normalized)) {
				continue;
			}
			ExperienceLevelEnum.fromValue(normalized)
					.orElseThrow(() -> new BusinessRuleException(
							"error.job.experience-level.invalid", value));
			resolved.add(normalized);
		}
		return resolved;
	}
}
