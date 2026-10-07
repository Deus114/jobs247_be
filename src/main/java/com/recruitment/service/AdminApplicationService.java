package com.recruitment.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.recruitment.dto.request.admin.application.AdminApplicationFilterRequest;
import com.recruitment.dto.response.common.ApiResponse;
import com.recruitment.dto.response.common.PaginatedResponse;
import com.recruitment.dto.response.user.application.JobApplicationResponse;
import com.recruitment.entity.JobApplication;
import com.recruitment.enums.ApplicationStatusEnum;
import com.recruitment.exception.BusinessRuleException;
import com.recruitment.exception.ResourceNotFoundException;
import com.recruitment.mapper.JobApplicationMapper;
import com.recruitment.repository.JobApplicationRepository;
import com.recruitment.specification.JobApplicationSpecification;
import com.recruitment.util.helper.AccentHelper;
import com.recruitment.util.helper.SoftDeletedPageQuery;

@Service
public class AdminApplicationService {

	private final JobApplicationRepository jobApplicationRepository;
	private final JobApplicationMapper jobApplicationMapper;
	private final SoftDeletedPageQuery softDeletedPageQuery;
	private final MessageService messageService;

	public AdminApplicationService(
			JobApplicationRepository jobApplicationRepository,
			JobApplicationMapper jobApplicationMapper,
			SoftDeletedPageQuery softDeletedPageQuery,
			MessageService messageService) {
		this.jobApplicationRepository = jobApplicationRepository;
		this.jobApplicationMapper = jobApplicationMapper;
		this.softDeletedPageQuery = softDeletedPageQuery;
		this.messageService = messageService;
	}

	@Transactional(readOnly = true)
	public ResponseEntity<ApiResponse<PaginatedResponse<JobApplicationResponse>>> getList(
			AdminApplicationFilterRequest filter,
			Pageable pageable) {
		validateStatus(filter.status());

		Page<JobApplication> applicationsPage = filter.isDeleted()
				? softDeletedPageQuery.find(
						JobApplication.class,
						"job_applications a JOIN jobs j ON a.job_id = j.id",
						"a",
						List.of("a.full_name", "a.email", "j.title"),
						AccentHelper.normalizeForSearch(filter.keyword()),
						"""
								AND (:status IS NULL OR a.status = :status)
								AND (:companyId IS NULL OR j.company_id = :companyId)
								AND (:jobId IS NULL OR a.job_id = :jobId)
								AND (:fromDate IS NULL OR a.created_at >= :fromDate)
								AND (:toDate IS NULL OR a.created_at < :toDate)
								""",
						SoftDeletedPageQuery.params(
								"status", blankToNullUpper(filter.status()),
								"companyId", filter.companyId(),
								"jobId", filter.jobId(),
								"fromDate", startOfDayUtc(filter.fromDate()),
								"toDate", startOfNextDayUtc(filter.toDate())),
						"a.created_at DESC, a.id DESC",
						pageable)
				: jobApplicationRepository.findAll(JobApplicationSpecification.buildAdmin(filter), pageable);

		Page<JobApplicationResponse> page = applicationsPage.map(jobApplicationMapper::toResponse);

		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("admin-application.list.success"),
				PaginatedResponse.fromPage(page)));
	}

	@Transactional(readOnly = true)
	public ResponseEntity<ApiResponse<JobApplicationResponse>> getById(Long id) {
		JobApplication application = jobApplicationRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("error.resource.application", "id", id));
		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("common.success"),
				jobApplicationMapper.toResponse(application)));
	}

	@Transactional
	public ResponseEntity<ApiResponse<Void>> softDelete(Long id, String currentUsername) {
		JobApplication application = jobApplicationRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("error.resource.application", "id", id));
		application.setDeletedBy(currentUsername);
		jobApplicationRepository.save(application);
		jobApplicationRepository.delete(application);
		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("admin-application.soft-delete.success"),
				null));
	}

	@Transactional
	public ResponseEntity<ApiResponse<Void>> restore(Long id, String currentUsername) {
		int restored = jobApplicationRepository.restoreById(id);
		if (restored == 0) {
			throw new ResourceNotFoundException("error.resource.application", "id", id);
		}
		JobApplication application = jobApplicationRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("error.resource.application", "id", id));
		application.setUpdatedBy(currentUsername);
		jobApplicationRepository.save(application);
		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("admin-application.restore.success"),
				null));
	}

	@Transactional
	public ResponseEntity<ApiResponse<Void>> permanentDelete(Long id) {
		jobApplicationRepository.findSoftDeletedById(id)
				.orElseThrow(() -> new ResourceNotFoundException("error.resource.application", "id", id));
		jobApplicationRepository.hardDeleteById(id);
		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("admin-application.permanent-delete.success"),
				null));
	}

	private void validateStatus(String status) {
		if (StringUtils.hasText(status)) {
			ApplicationStatusEnum.fromValue(status)
					.orElseThrow(() -> new BusinessRuleException("error.application.status.invalid", status));
		}
	}

	private String blankToNullUpper(String value) {
		if (!StringUtils.hasText(value)) {
			return null;
		}
		return value.trim().toUpperCase();
	}

	private Instant startOfDayUtc(LocalDate date) {
		if (date == null) {
			return null;
		}
		return date.atStartOfDay().toInstant(ZoneOffset.UTC);
	}

	private Instant startOfNextDayUtc(LocalDate date) {
		if (date == null) {
			return null;
		}
		return date.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
	}
}
