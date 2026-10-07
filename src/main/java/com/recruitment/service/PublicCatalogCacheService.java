package com.recruitment.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.recruitment.dto.response.common.BusinessConfigPublicResponse;
import com.recruitment.dto.response.common.EducationLevelPublicResponse;
import com.recruitment.dto.response.common.IndustryGroupPublicResponse;
import com.recruitment.dto.response.common.PaginatedResponse;
import com.recruitment.dto.response.common.ProvincePublicResponse;
import com.recruitment.entity.BusinessConfig;
import com.recruitment.exception.ResourceNotFoundException;
import com.recruitment.mapper.BusinessConfigMapper;
import com.recruitment.mapper.EducationLevelMapper;
import com.recruitment.mapper.IndustryGroupMapper;
import com.recruitment.mapper.ProvinceMapper;
import com.recruitment.repository.BusinessConfigRepository;
import com.recruitment.repository.EducationLevelRepository;
import com.recruitment.repository.IndustryGroupRepository;
import com.recruitment.repository.ProvinceRepository;
import com.recruitment.specification.EducationLevelSpecification;
import com.recruitment.specification.IndustryGroupSpecification;
import com.recruitment.specification.ProvinceSpecification;
import com.recruitment.util.constant.CacheNames;
import com.recruitment.util.constant.Constants;

@Service
public class PublicCatalogCacheService {

	private final ProvinceRepository provinceRepository;
	private final ProvinceMapper provinceMapper;
	private final IndustryGroupRepository industryGroupRepository;
	private final IndustryGroupMapper industryGroupMapper;
	private final EducationLevelRepository educationLevelRepository;
	private final EducationLevelMapper educationLevelMapper;
	private final BusinessConfigRepository businessConfigRepository;
	private final BusinessConfigMapper businessConfigMapper;

	public PublicCatalogCacheService(
			ProvinceRepository provinceRepository,
			ProvinceMapper provinceMapper,
			IndustryGroupRepository industryGroupRepository,
			IndustryGroupMapper industryGroupMapper,
			EducationLevelRepository educationLevelRepository,
			EducationLevelMapper educationLevelMapper,
			BusinessConfigRepository businessConfigRepository,
			BusinessConfigMapper businessConfigMapper) {
		this.provinceRepository = provinceRepository;
		this.provinceMapper = provinceMapper;
		this.industryGroupRepository = industryGroupRepository;
		this.industryGroupMapper = industryGroupMapper;
		this.educationLevelRepository = educationLevelRepository;
		this.educationLevelMapper = educationLevelMapper;
		this.businessConfigRepository = businessConfigRepository;
		this.businessConfigMapper = businessConfigMapper;
	}

	@Cacheable(cacheNames = CacheNames.PROVINCES, key = "T(com.recruitment.util.helper.CacheKeyHelper).pageable(#pageable)")
	@Transactional(readOnly = true)
	public PaginatedResponse<ProvincePublicResponse> provinces(Pageable pageable) {
		return PaginatedResponse.fromPage(provinceRepository
				.findAll(ProvinceSpecification.publicActive(), pageable)
				.map(provinceMapper::toPublicResponse));
	}

	@Cacheable(cacheNames = CacheNames.INDUSTRY_GROUPS, key = "T(com.recruitment.util.helper.CacheKeyHelper).pageable(#pageable)")
	@Transactional(readOnly = true)
	public PaginatedResponse<IndustryGroupPublicResponse> industryGroups(Pageable pageable) {
		return PaginatedResponse.fromPage(industryGroupRepository
				.findAll(IndustryGroupSpecification.publicActive(), pageable)
				.map(industryGroupMapper::toPublicResponse));
	}

	@Cacheable(cacheNames = CacheNames.EDUCATION_LEVELS, key = "T(com.recruitment.util.helper.CacheKeyHelper).pageable(#pageable)")
	@Transactional(readOnly = true)
	public PaginatedResponse<EducationLevelPublicResponse> educationLevels(Pageable pageable) {
		return PaginatedResponse.fromPage(educationLevelRepository
				.findAll(EducationLevelSpecification.publicActive(), pageable)
				.map(educationLevelMapper::toPublicResponse));
	}

	@Cacheable(cacheNames = CacheNames.BUSINESS_CONFIG, key = "'singleton'")
	@Transactional(readOnly = true)
	public BusinessConfigPublicResponse businessConfig() {
		BusinessConfig config = businessConfigRepository.findById(Constants.BUSINESS_CONFIG_ID)
				.orElseThrow(() -> new ResourceNotFoundException(
						"error.resource.business-config",
						"id",
						Constants.BUSINESS_CONFIG_ID));
		return businessConfigMapper.toPublicResponse(config);
	}
}
