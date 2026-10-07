package com.recruitment.service;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.recruitment.dto.request.admin.educationlevel.CreateEducationLevelRequest;
import com.recruitment.dto.request.admin.educationlevel.EducationLevelFilterRequest;
import com.recruitment.dto.request.admin.educationlevel.UpdateEducationLevelRequest;
import com.recruitment.dto.response.admin.educationlevel.EducationLevelResponse;
import com.recruitment.dto.response.common.ApiResponse;
import com.recruitment.dto.response.common.EducationLevelPublicResponse;
import com.recruitment.dto.response.common.PaginatedResponse;
import com.recruitment.entity.EducationLevel;
import com.recruitment.exception.BusinessRuleException;
import com.recruitment.exception.DuplicateResourceException;
import com.recruitment.exception.ResourceNotFoundException;
import com.recruitment.mapper.EducationLevelMapper;
import com.recruitment.repository.EducationLevelRepository;
import com.recruitment.specification.EducationLevelSpecification;
import com.recruitment.util.constant.CacheNames;
import com.recruitment.util.helper.AccentHelper;
import com.recruitment.util.helper.SoftDeletedPageQuery;

@Service
public class EducationLevelService {

	private final EducationLevelRepository educationLevelRepository;
	private final EducationLevelMapper educationLevelMapper;
	private final SoftDeletedPageQuery softDeletedPageQuery;
	private final PublicCatalogCacheService publicCatalogCacheService;
	private final MessageService messageService;

	public EducationLevelService(
			EducationLevelRepository educationLevelRepository,
			EducationLevelMapper educationLevelMapper,
			SoftDeletedPageQuery softDeletedPageQuery,
			PublicCatalogCacheService publicCatalogCacheService,
			MessageService messageService) {
		this.educationLevelRepository = educationLevelRepository;
		this.educationLevelMapper = educationLevelMapper;
		this.softDeletedPageQuery = softDeletedPageQuery;
		this.publicCatalogCacheService = publicCatalogCacheService;
		this.messageService = messageService;
	}

	@Transactional(readOnly = true)
	public ResponseEntity<ApiResponse<PaginatedResponse<EducationLevelPublicResponse>>> getPublicList(
			Pageable pageable) {
		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("education-level.public-list.success"),
				publicCatalogCacheService.educationLevels(pageable)));
	}

	@Transactional(readOnly = true)
	public ResponseEntity<ApiResponse<PaginatedResponse<EducationLevelResponse>>> getList(
			EducationLevelFilterRequest filter,
			Pageable pageable) {

		Page<EducationLevelResponse> resultPage = filter.isDeleted()
				? softDeletedPageQuery.find(
						EducationLevel.class,
						"education_levels e",
						"e",
						List.of("e.name_vi", "e.name_en"),
						AccentHelper.normalizeForSearch(filter.keyword()),
						"AND (:active IS NULL OR e.active = :active)",
						SoftDeletedPageQuery.params("active", filter.active()),
						"e.sort_order ASC, e.id ASC",
						pageable)
						.map(educationLevelMapper::toAdminResponse)
				: educationLevelRepository.findAll(EducationLevelSpecification.build(filter), pageable)
						.map(educationLevelMapper::toAdminResponse);

		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("education-level.list.success"),
				PaginatedResponse.fromPage(resultPage)));
	}

	public ResponseEntity<ApiResponse<EducationLevelResponse>> getById(Long id) {
		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("common.success"),
				educationLevelMapper.toAdminResponse(findActiveById(id))));
	}

	@Transactional
	@CacheEvict(cacheNames = CacheNames.EDUCATION_LEVELS, allEntries = true)
	public ResponseEntity<ApiResponse<EducationLevelResponse>> create(
			CreateEducationLevelRequest request,
			String currentUsername) {

		validateNameViUnique(request.nameVi(), null);
		validateNameEnUnique(request.nameEn(), null);

		EducationLevel educationLevel = new EducationLevel();
		educationLevel.setNameVi(request.nameVi().trim());
		educationLevel.setNameEn(request.nameEn().trim());
		educationLevel.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
		educationLevel.setActive(request.active() == null || request.active());
		educationLevel.setCreatedBy(currentUsername);
		educationLevel.setUpdatedBy(currentUsername);

		EducationLevel saved = educationLevelRepository.save(educationLevel);

		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(
				HttpStatus.CREATED.value(),
				messageService.get("education-level.create.success"),
				educationLevelMapper.toAdminResponse(saved)));
	}

	@Transactional
	@CacheEvict(cacheNames = CacheNames.EDUCATION_LEVELS, allEntries = true)
	public ResponseEntity<ApiResponse<EducationLevelResponse>> update(
			Long id,
			UpdateEducationLevelRequest request,
			String currentUsername) {

		EducationLevel educationLevel = findActiveById(id);

		if (StringUtils.hasText(request.nameVi())) {
			validateNameViUnique(request.nameVi(), id);
			educationLevel.setNameVi(request.nameVi().trim());
		}
		if (StringUtils.hasText(request.nameEn())) {
			validateNameEnUnique(request.nameEn(), id);
			educationLevel.setNameEn(request.nameEn().trim());
		}
		if (request.sortOrder() != null) {
			educationLevel.setSortOrder(request.sortOrder());
		}
		if (request.active() != null) {
			educationLevel.setActive(request.active());
		}
		educationLevel.setUpdatedBy(currentUsername);

		EducationLevel saved = educationLevelRepository.save(educationLevel);

		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("education-level.update.success"),
				educationLevelMapper.toAdminResponse(saved)));
	}

	@Transactional
	@CacheEvict(cacheNames = CacheNames.EDUCATION_LEVELS, allEntries = true)
	public ResponseEntity<ApiResponse<Void>> softDelete(Long id, String currentUsername) {
		EducationLevel educationLevel = findActiveById(id);
		educationLevel.setDeletedBy(currentUsername);
		educationLevelRepository.save(educationLevel);
		educationLevelRepository.delete(educationLevel);

		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("education-level.soft-delete.success"),
				null));
	}

	@Transactional
	@CacheEvict(cacheNames = CacheNames.EDUCATION_LEVELS, allEntries = true)
	public ResponseEntity<ApiResponse<Void>> restore(Long id, String currentUsername) {
		int restored = educationLevelRepository.restoreById(id);
		if (restored == 0) {
			throw new ResourceNotFoundException("error.resource.education-level", "id", id);
		}

		EducationLevel educationLevel = findActiveById(id);
		educationLevel.setUpdatedBy(currentUsername);
		educationLevelRepository.save(educationLevel);

		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("education-level.restore.success"),
				null));
	}

	@Transactional
	@CacheEvict(cacheNames = CacheNames.EDUCATION_LEVELS, allEntries = true)
	public ResponseEntity<ApiResponse<Void>> permanentDelete(Long id) {
		educationLevelRepository.findSoftDeletedById(id)
				.orElseThrow(() -> new BusinessRuleException("error.education-level.not-soft-deleted", id));

		educationLevelRepository.hardDeleteById(id);

		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("education-level.permanent-delete.success"),
				null));
	}

	private EducationLevel findActiveById(Long id) {
		return educationLevelRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("error.resource.education-level", "id", id));
	}

	private void validateNameViUnique(String nameVi, Long excludeId) {
		boolean exists = excludeId == null
				? educationLevelRepository.existsByNameVi(nameVi.trim())
				: educationLevelRepository.existsByNameViAndIdNot(nameVi.trim(), excludeId);
		if (exists) {
			throw new DuplicateResourceException("error.education-level.name-vi.duplicate", nameVi.trim());
		}
	}

	private void validateNameEnUnique(String nameEn, Long excludeId) {
		boolean exists = excludeId == null
				? educationLevelRepository.existsByNameEn(nameEn.trim())
				: educationLevelRepository.existsByNameEnAndIdNot(nameEn.trim(), excludeId);
		if (exists) {
			throw new DuplicateResourceException("error.education-level.name-en.duplicate", nameEn.trim());
		}
	}
}
