package com.recruitment.service;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.recruitment.dto.request.admin.industrygroup.CreateIndustryGroupRequest;
import com.recruitment.dto.request.admin.industrygroup.IndustryGroupFilterRequest;
import com.recruitment.dto.request.admin.industrygroup.UpdateIndustryGroupRequest;
import com.recruitment.dto.response.admin.industrygroup.IndustryGroupResponse;
import com.recruitment.dto.response.common.ApiResponse;
import com.recruitment.dto.response.common.IndustryGroupPublicResponse;
import com.recruitment.dto.response.common.PaginatedResponse;
import com.recruitment.dto.response.common.UploadedFileResponse;
import com.recruitment.entity.IndustryGroup;
import com.recruitment.enums.UploadFolderType;
import com.recruitment.exception.BusinessRuleException;
import com.recruitment.exception.ResourceNotFoundException;
import com.recruitment.mapper.IndustryGroupMapper;
import com.recruitment.repository.IndustryGroupRepository;
import com.recruitment.repository.IndustryRepository;
import com.recruitment.specification.IndustryGroupSpecification;
import com.recruitment.util.constant.CacheNames;
import com.recruitment.util.helper.AccentHelper;
import com.recruitment.util.helper.FileHelper;
import com.recruitment.util.helper.SoftDeletedPageQuery;

@Service
public class IndustryGroupService {

	private static final String SORT_INDUSTRY_COUNT = "industryCount";

	private final IndustryGroupRepository industryGroupRepository;
	private final IndustryRepository industryRepository;
	private final IndustryGroupMapper industryGroupMapper;
	private final FileStorageService fileStorageService;
	private final SoftDeletedPageQuery softDeletedPageQuery;
	private final PublicCatalogCacheService publicCatalogCacheService;
	private final MessageService messageService;

	public IndustryGroupService(
			IndustryGroupRepository industryGroupRepository,
			IndustryRepository industryRepository,
			IndustryGroupMapper industryGroupMapper,
			FileStorageService fileStorageService,
			SoftDeletedPageQuery softDeletedPageQuery,
			PublicCatalogCacheService publicCatalogCacheService,
			MessageService messageService) {
		this.industryGroupRepository = industryGroupRepository;
		this.industryRepository = industryRepository;
		this.industryGroupMapper = industryGroupMapper;
		this.fileStorageService = fileStorageService;
		this.softDeletedPageQuery = softDeletedPageQuery;
		this.publicCatalogCacheService = publicCatalogCacheService;
		this.messageService = messageService;
	}

	@Transactional(readOnly = true)
	public ResponseEntity<ApiResponse<PaginatedResponse<IndustryGroupPublicResponse>>> getPublicList(
			Pageable pageable) {
		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("industry-group.public-list.success"),
				publicCatalogCacheService.industryGroups(pageable)));
	}

	@Transactional(readOnly = true)
	public ResponseEntity<ApiResponse<PaginatedResponse<IndustryGroupResponse>>> getList(
			IndustryGroupFilterRequest filter,
			Pageable pageable) {

		Page<IndustryGroupResponse> resultPage = filter.isDeleted()
				? softDeletedPageQuery.find(
						IndustryGroup.class,
						"industry_groups g",
						"g",
						List.of("g.name_vi", "g.name_en", "g.description_vi", "g.description_en"),
						AccentHelper.normalizeForSearch(filter.keyword()),
						"AND (:active IS NULL OR g.active = :active)",
						SoftDeletedPageQuery.params("active", filter.active()),
						resolveSoftDeletedOrderBy(pageable),
						pageable)
						.map(group -> industryGroupMapper.toAdminResponse(
								group,
								industryRepository.countAllByIndustryGroupId(group.getId())))
				: findActivePage(filter, pageable)
						.map(group -> industryGroupMapper.toAdminResponse(
								group,
								industryRepository.countByIndustryGroupId(group.getId())));

		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("industry-group.list.success"),
				PaginatedResponse.fromPage(resultPage)));
	}

	private Page<IndustryGroup> findActivePage(IndustryGroupFilterRequest filter, Pageable pageable) {
		Sort.Order industryCountOrder = pageable.getSort().getOrderFor(SORT_INDUSTRY_COUNT);
		if (industryCountOrder != null) {
			Specification<IndustryGroup> spec = IndustryGroupSpecification.build(filter)
					.and(IndustryGroupSpecification.orderByIndustryCount(industryCountOrder.isAscending()));
			Pageable unsorted = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
			return industryGroupRepository.findAll(spec, unsorted);
		}
		return industryGroupRepository.findAll(IndustryGroupSpecification.build(filter), pageable);
	}

	private static String resolveSoftDeletedOrderBy(Pageable pageable) {
		Sort.Order industryCountOrder = pageable.getSort().getOrderFor(SORT_INDUSTRY_COUNT);
		if (industryCountOrder != null) {
			String direction = industryCountOrder.isAscending() ? "ASC" : "DESC";
			return "(SELECT COUNT(*) FROM industries i WHERE i.industry_group_id = g.id) " + direction
					+ ", g.id ASC";
		}
		return "g.sort_order ASC, g.id ASC";
	}

	public ResponseEntity<ApiResponse<IndustryGroupResponse>> getById(Long id) {
		IndustryGroup group = findActiveById(id);
		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("common.success"),
				industryGroupMapper.toAdminResponse(
						group,
						industryRepository.countByIndustryGroupId(group.getId()))));
	}

	@Transactional
	@CacheEvict(cacheNames = CacheNames.INDUSTRY_GROUPS, allEntries = true)
	public ResponseEntity<ApiResponse<IndustryGroupResponse>> create(
			CreateIndustryGroupRequest request,
			String currentUsername) {

		IndustryGroup group = new IndustryGroup();
		group.setNameVi(request.nameVi().trim());
		group.setNameEn(request.nameEn().trim());
		group.setDescriptionVi(trimToNull(request.descriptionVi()));
		group.setDescriptionEn(trimToNull(request.descriptionEn()));
		group.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
		group.setActive(request.active() == null || request.active());
		group.setCreatedBy(currentUsername);
		group.setUpdatedBy(currentUsername);

		applyImageField(
				request.imageFile(),
				request.image(),
				group::getImage,
				group::setImage,
				UploadFolderType.INDUSTRY_GROUP);

		IndustryGroup saved = industryGroupRepository.save(group);

		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(
				HttpStatus.CREATED.value(),
				messageService.get("industry-group.create.success"),
				industryGroupMapper.toAdminResponse(saved, 0L)));
	}

	@Transactional
	@CacheEvict(cacheNames = CacheNames.INDUSTRY_GROUPS, allEntries = true)
	public ResponseEntity<ApiResponse<IndustryGroupResponse>> update(
			Long id,
			UpdateIndustryGroupRequest request,
			String currentUsername) {

		IndustryGroup group = findActiveById(id);

		if (StringUtils.hasText(request.nameVi())) {
			group.setNameVi(request.nameVi().trim());
		}
		if (StringUtils.hasText(request.nameEn())) {
			group.setNameEn(request.nameEn().trim());
		}
		if (request.descriptionVi() != null) {
			group.setDescriptionVi(trimToNull(request.descriptionVi()));
		}
		if (request.descriptionEn() != null) {
			group.setDescriptionEn(trimToNull(request.descriptionEn()));
		}
		if (request.sortOrder() != null) {
			group.setSortOrder(request.sortOrder());
		}
		if (request.active() != null) {
			group.setActive(request.active());
		}

		applyImageField(
				request.imageFile(),
				request.image(),
				group::getImage,
				group::setImage,
				UploadFolderType.INDUSTRY_GROUP);

		group.setUpdatedBy(currentUsername);
		IndustryGroup saved = industryGroupRepository.save(group);

		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("industry-group.update.success"),
				industryGroupMapper.toAdminResponse(
						saved,
						industryRepository.countByIndustryGroupId(saved.getId()))));
	}

	@Transactional
	@CacheEvict(cacheNames = CacheNames.INDUSTRY_GROUPS, allEntries = true)
	public ResponseEntity<ApiResponse<Void>> softDelete(Long id, String currentUsername) {
		IndustryGroup group = findActiveById(id);

		if (industryRepository.existsByIndustryGroupId(id)) {
			throw new BusinessRuleException("error.industry-group.in-use", group.getNameVi());
		}

		group.setDeletedBy(currentUsername);
		industryGroupRepository.save(group);
		industryGroupRepository.delete(group);

		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("industry-group.soft-delete.success"),
				null));
	}

	@Transactional
	@CacheEvict(cacheNames = CacheNames.INDUSTRY_GROUPS, allEntries = true)
	public ResponseEntity<ApiResponse<Void>> restore(Long id, String currentUsername) {
		int restored = industryGroupRepository.restoreById(id);
		if (restored == 0) {
			throw new ResourceNotFoundException("error.resource.industry-group", "id", id);
		}

		IndustryGroup group = findActiveById(id);
		group.setUpdatedBy(currentUsername);
		industryGroupRepository.save(group);

		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("industry-group.restore.success"),
				null));
	}

	@Transactional
	@CacheEvict(cacheNames = CacheNames.INDUSTRY_GROUPS, allEntries = true)
	public ResponseEntity<ApiResponse<Void>> permanentDelete(Long id) {
		industryGroupRepository.findSoftDeletedById(id)
				.orElseThrow(() -> new BusinessRuleException("error.industry-group.not-soft-deleted", id));

		if (industryRepository.countAllByIndustryGroupId(id) > 0) {
			throw new BusinessRuleException("error.industry-group.in-use-permanent", id);
		}

		industryGroupRepository.hardDeleteById(id);

		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("industry-group.permanent-delete.success"),
				null));
	}

	private IndustryGroup findActiveById(Long id) {
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
