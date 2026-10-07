package com.recruitment.service;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.recruitment.dto.request.admin.industry.CreateIndustryRequest;
import com.recruitment.dto.request.admin.industry.IndustryFilterRequest;
import com.recruitment.dto.request.admin.industry.UpdateIndustryRequest;
import com.recruitment.dto.response.admin.industry.IndustryResponse;
import com.recruitment.dto.response.common.ApiResponse;
import com.recruitment.dto.response.common.PaginatedResponse;
import com.recruitment.dto.response.common.UploadedFileResponse;
import com.recruitment.entity.Industry;
import com.recruitment.entity.IndustryGroup;
import com.recruitment.enums.UploadFolderType;
import com.recruitment.exception.BusinessRuleException;
import com.recruitment.exception.ResourceNotFoundException;
import com.recruitment.mapper.IndustryMapper;
import com.recruitment.repository.IndustryGroupRepository;
import com.recruitment.repository.IndustryRepository;
import com.recruitment.specification.IndustrySpecification;
import com.recruitment.util.constant.CacheNames;
import com.recruitment.util.helper.AccentHelper;
import com.recruitment.util.helper.FileHelper;
import com.recruitment.util.helper.SoftDeletedPageQuery;

@Service
public class IndustryService {

	private final IndustryRepository industryRepository;
	private final IndustryGroupRepository industryGroupRepository;
	private final IndustryMapper industryMapper;
	private final FileStorageService fileStorageService;
	private final SoftDeletedPageQuery softDeletedPageQuery;
	private final MessageService messageService;

	public IndustryService(
			IndustryRepository industryRepository,
			IndustryGroupRepository industryGroupRepository,
			IndustryMapper industryMapper,
			FileStorageService fileStorageService,
			SoftDeletedPageQuery softDeletedPageQuery,
			MessageService messageService) {
		this.industryRepository = industryRepository;
		this.industryGroupRepository = industryGroupRepository;
		this.industryMapper = industryMapper;
		this.fileStorageService = fileStorageService;
		this.softDeletedPageQuery = softDeletedPageQuery;
		this.messageService = messageService;
	}

	@Transactional(readOnly = true)
	public ResponseEntity<ApiResponse<PaginatedResponse<IndustryResponse>>> getList(
			IndustryFilterRequest filter,
			Pageable pageable) {

		Page<IndustryResponse> resultPage = filter.isDeleted()
				? softDeletedPageQuery.find(
						Industry.class,
						"industries i",
						"i",
						List.of("i.name_vi", "i.name_en", "i.description_vi", "i.description_en"),
						AccentHelper.normalizeForSearch(filter.keyword()),
						"AND (:active IS NULL OR i.active = :active) "
								+ "AND (:industryGroupId IS NULL OR i.industry_group_id = :industryGroupId)",
						SoftDeletedPageQuery.params(
								"active", filter.active(),
								"industryGroupId", filter.industryGroupId()),
						"i.sort_order ASC, i.id ASC",
						pageable)
						.map(industryMapper::toAdminResponse)
				: industryRepository.findAll(IndustrySpecification.build(filter), pageable)
						.map(industryMapper::toAdminResponse);

		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("industry.list.success"),
				PaginatedResponse.fromPage(resultPage)));
	}

	public ResponseEntity<ApiResponse<IndustryResponse>> getById(Long id) {
		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("common.success"),
				industryMapper.toAdminResponse(findActiveByIdWithGroup(id))));
	}

	@Transactional
	@CacheEvict(cacheNames = CacheNames.INDUSTRY_GROUPS, allEntries = true)
	public ResponseEntity<ApiResponse<IndustryResponse>> create(
			CreateIndustryRequest request,
			String currentUsername) {

		IndustryGroup group = findActiveGroup(request.industryGroupId());

		Industry industry = new Industry();
		industry.setIndustryGroup(group);
		industry.setNameVi(request.nameVi().trim());
		industry.setNameEn(request.nameEn().trim());
		industry.setDescriptionVi(trimToNull(request.descriptionVi()));
		industry.setDescriptionEn(trimToNull(request.descriptionEn()));
		industry.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
		industry.setActive(request.active() == null || request.active());
		industry.setCreatedBy(currentUsername);
		industry.setUpdatedBy(currentUsername);

		applyImageField(
				request.imageFile(),
				request.image(),
				industry::getImage,
				industry::setImage,
				UploadFolderType.INDUSTRY);

		Industry saved = industryRepository.save(industry);

		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(
				HttpStatus.CREATED.value(),
				messageService.get("industry.create.success"),
				industryMapper.toAdminResponse(findActiveByIdWithGroup(saved.getId()))));
	}

	@Transactional
	@CacheEvict(cacheNames = CacheNames.INDUSTRY_GROUPS, allEntries = true)
	public ResponseEntity<ApiResponse<IndustryResponse>> update(
			Long id,
			UpdateIndustryRequest request,
			String currentUsername) {

		Industry industry = findActiveByIdWithGroup(id);

		if (request.industryGroupId() != null
				&& !request.industryGroupId().equals(industry.getIndustryGroup().getId())) {
			industry.setIndustryGroup(findActiveGroup(request.industryGroupId()));
		}
		if (StringUtils.hasText(request.nameVi())) {
			industry.setNameVi(request.nameVi().trim());
		}
		if (StringUtils.hasText(request.nameEn())) {
			industry.setNameEn(request.nameEn().trim());
		}
		if (request.descriptionVi() != null) {
			industry.setDescriptionVi(trimToNull(request.descriptionVi()));
		}
		if (request.descriptionEn() != null) {
			industry.setDescriptionEn(trimToNull(request.descriptionEn()));
		}
		if (request.sortOrder() != null) {
			industry.setSortOrder(request.sortOrder());
		}
		if (request.active() != null) {
			industry.setActive(request.active());
		}

		applyImageField(
				request.imageFile(),
				request.image(),
				industry::getImage,
				industry::setImage,
				UploadFolderType.INDUSTRY);

		industry.setUpdatedBy(currentUsername);
		Industry saved = industryRepository.save(industry);

		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("industry.update.success"),
				industryMapper.toAdminResponse(findActiveByIdWithGroup(saved.getId()))));
	}

	@Transactional
	@CacheEvict(cacheNames = CacheNames.INDUSTRY_GROUPS, allEntries = true)
	public ResponseEntity<ApiResponse<Void>> softDelete(Long id, String currentUsername) {
		Industry industry = findActiveById(id);
		industry.setDeletedBy(currentUsername);
		industryRepository.save(industry);
		industryRepository.delete(industry);

		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("industry.soft-delete.success"),
				null));
	}

	@Transactional
	@CacheEvict(cacheNames = CacheNames.INDUSTRY_GROUPS, allEntries = true)
	public ResponseEntity<ApiResponse<Void>> restore(Long id, String currentUsername) {
		int restored = industryRepository.restoreById(id);
		if (restored == 0) {
			throw new ResourceNotFoundException("error.resource.industry", "id", id);
		}

		Industry industry = findActiveByIdWithGroup(id);
		if (industry.getIndustryGroup() == null) {
			throw new BusinessRuleException("error.industry.group-missing", id);
		}

		industry.setUpdatedBy(currentUsername);
		industryRepository.save(industry);

		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("industry.restore.success"),
				null));
	}

	@Transactional
	@CacheEvict(cacheNames = CacheNames.INDUSTRY_GROUPS, allEntries = true)
	public ResponseEntity<ApiResponse<Void>> permanentDelete(Long id) {
		industryRepository.findSoftDeletedById(id)
				.orElseThrow(() -> new BusinessRuleException("error.industry.not-soft-deleted", id));

		industryRepository.hardDeleteById(id);

		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("industry.permanent-delete.success"),
				null));
	}

	private Industry findActiveById(Long id) {
		return industryRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("error.resource.industry", "id", id));
	}

	private Industry findActiveByIdWithGroup(Long id) {
		return industryRepository.findByIdWithGroup(id)
				.orElseThrow(() -> new ResourceNotFoundException("error.resource.industry", "id", id));
	}

	private IndustryGroup findActiveGroup(Long id) {
		return industryGroupRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("error.resource.industry-group", "id", id));
	}

	private void applyImageField(
			MultipartFile file,
			String link,
			Supplier<String> currentGetter,
			Consumer<String> setter,
			UploadFolderType folderType) {

		boolean hasFile = FileHelper.hasFile(file);
		boolean hasLink = StringUtils.hasText(link);

		if (hasFile && hasLink) {
			throw new BusinessRuleException("error.business-config.image.file-and-link");
		}

		if (hasFile) {
			String oldUrl = currentGetter.get();
			UploadedFileResponse uploaded = fileStorageService.uploadImage(file, folderType);
			setter.accept(uploaded.url());
			if (oldUrl != null && !oldUrl.equals(uploaded.url())) {
				fileStorageService.deleteByPublicUrl(oldUrl);
			}
			return;
		}

		if (link != null) {
			setter.accept(trimToNull(link));
		}
	}

	private String trimToNull(String value) {
		if (!StringUtils.hasText(value)) {
			return null;
		}
		return value.trim();
	}
}
