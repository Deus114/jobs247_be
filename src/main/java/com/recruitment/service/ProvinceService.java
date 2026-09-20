package com.recruitment.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.recruitment.dto.request.admin.province.CreateProvinceRequest;
import com.recruitment.dto.request.admin.province.ProvinceFilterRequest;
import com.recruitment.dto.request.admin.province.UpdateProvinceRequest;
import com.recruitment.dto.response.admin.province.ProvinceResponse;
import com.recruitment.dto.response.common.ApiResponse;
import com.recruitment.dto.response.common.PaginatedResponse;
import com.recruitment.dto.response.common.ProvincePublicResponse;
import com.recruitment.entity.Province;
import com.recruitment.enums.ProvinceRegionEnum;
import com.recruitment.exception.BusinessRuleException;
import com.recruitment.exception.DuplicateResourceException;
import com.recruitment.exception.ResourceNotFoundException;
import com.recruitment.mapper.ProvinceMapper;
import com.recruitment.repository.ProvinceRepository;
import com.recruitment.specification.ProvinceSpecification;
import com.recruitment.util.helper.AccentHelper;
import com.recruitment.util.helper.SoftDeletedPageQuery;

@Service
public class ProvinceService {

	private final ProvinceRepository provinceRepository;
	private final ProvinceMapper provinceMapper;
	private final SoftDeletedPageQuery softDeletedPageQuery;
	private final MessageService messageService;

	public ProvinceService(
			ProvinceRepository provinceRepository,
			ProvinceMapper provinceMapper,
			SoftDeletedPageQuery softDeletedPageQuery,
			MessageService messageService) {
		this.provinceRepository = provinceRepository;
		this.provinceMapper = provinceMapper;
		this.softDeletedPageQuery = softDeletedPageQuery;
		this.messageService = messageService;
	}

	@Transactional(readOnly = true)
	public ResponseEntity<ApiResponse<PaginatedResponse<ProvincePublicResponse>>> getPublicList(
			Pageable pageable) {
		Page<ProvincePublicResponse> resultPage = provinceRepository
				.findAll(ProvinceSpecification.publicActive(), pageable)
				.map(provinceMapper::toPublicResponse);

		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("province.public-list.success"),
				PaginatedResponse.fromPage(resultPage)));
	}

	@Transactional(readOnly = true)
	public ResponseEntity<ApiResponse<PaginatedResponse<ProvinceResponse>>> getList(
			ProvinceFilterRequest filter,
			Pageable pageable) {

		Page<ProvinceResponse> resultPage = filter.isDeleted()
				? softDeletedPageQuery.find(
						Province.class,
						"provinces p",
						"p",
						List.of("p.name"),
						AccentHelper.normalizeForSearch(filter.keyword()),
						"AND (:region IS NULL OR p.region = :region) "
								+ "AND (:active IS NULL OR p.active = :active)",
						SoftDeletedPageQuery.params(
								"region", filter.normalizedRegion(),
								"active", filter.active()),
						"p.sort_order ASC, p.id ASC",
						pageable)
						.map(provinceMapper::toAdminResponse)
				: provinceRepository.findAll(ProvinceSpecification.build(filter), pageable)
						.map(provinceMapper::toAdminResponse);

		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("province.list.success"),
				PaginatedResponse.fromPage(resultPage)));
	}

	public ResponseEntity<ApiResponse<ProvinceResponse>> getById(Long id) {
		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("common.success"),
				provinceMapper.toAdminResponse(findActiveById(id))));
	}

	@Transactional
	public ResponseEntity<ApiResponse<ProvinceResponse>> create(
			CreateProvinceRequest request,
			String currentUsername) {

		validateNameUnique(request.name(), null);

		Province province = new Province();
		province.setName(request.name().trim());
		province.setRegion(normalizeOptionalRegion(request.region()));
		province.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
		province.setActive(request.active() == null || request.active());
		province.setCreatedBy(currentUsername);
		province.setUpdatedBy(currentUsername);

		Province saved = provinceRepository.save(province);

		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(
				HttpStatus.CREATED.value(),
				messageService.get("province.create.success"),
				provinceMapper.toAdminResponse(saved)));
	}

	@Transactional
	public ResponseEntity<ApiResponse<ProvinceResponse>> update(
			Long id,
			UpdateProvinceRequest request,
			String currentUsername) {

		Province province = findActiveById(id);

		if (StringUtils.hasText(request.name())) {
			validateNameUnique(request.name(), id);
			province.setName(request.name().trim());
		}
		if (request.region() != null) {
			province.setRegion(normalizeOptionalRegion(request.region()));
		}
		if (request.sortOrder() != null) {
			province.setSortOrder(request.sortOrder());
		}
		if (request.active() != null) {
			province.setActive(request.active());
		}
		province.setUpdatedBy(currentUsername);

		Province saved = provinceRepository.save(province);

		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("province.update.success"),
				provinceMapper.toAdminResponse(saved)));
	}

	@Transactional
	public ResponseEntity<ApiResponse<Void>> softDelete(Long id, String currentUsername) {
		Province province = findActiveById(id);
		province.setDeletedBy(currentUsername);
		provinceRepository.save(province);
		provinceRepository.delete(province);

		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("province.soft-delete.success"),
				null));
	}

	@Transactional
	public ResponseEntity<ApiResponse<Void>> restore(Long id, String currentUsername) {
		int restored = provinceRepository.restoreById(id);
		if (restored == 0) {
			throw new ResourceNotFoundException("error.resource.province", "id", id);
		}

		Province province = findActiveById(id);
		province.setUpdatedBy(currentUsername);
		provinceRepository.save(province);

		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("province.restore.success"),
				null));
	}

	@Transactional
	public ResponseEntity<ApiResponse<Void>> permanentDelete(Long id) {
		provinceRepository.findSoftDeletedById(id)
				.orElseThrow(() -> new BusinessRuleException("error.province.not-soft-deleted", id));

		provinceRepository.hardDeleteById(id);

		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("province.permanent-delete.success"),
				null));
	}

	private Province findActiveById(Long id) {
		return provinceRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("error.resource.province", "id", id));
	}

	private void validateNameUnique(String name, Long excludeId) {
		boolean exists = excludeId == null
				? provinceRepository.existsByName(name.trim())
				: provinceRepository.existsByNameAndIdNot(name.trim(), excludeId);
		if (exists) {
			throw new DuplicateResourceException("error.province.name.duplicate", name.trim());
		}
	}

	private String normalizeOptionalRegion(String region) {
		if (!StringUtils.hasText(region)) {
			return null;
		}
		return ProvinceRegionEnum.fromValue(region)
				.orElseThrow(() -> new BusinessRuleException("error.province.region.invalid", region))
				.name();
	}
}