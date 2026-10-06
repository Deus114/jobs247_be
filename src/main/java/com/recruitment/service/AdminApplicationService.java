package com.recruitment.service;

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

@Service
public class AdminApplicationService {

	private final JobApplicationRepository jobApplicationRepository;
	private final JobApplicationMapper jobApplicationMapper;
	private final MessageService messageService;

	public AdminApplicationService(
			JobApplicationRepository jobApplicationRepository,
			JobApplicationMapper jobApplicationMapper,
			MessageService messageService) {
		this.jobApplicationRepository = jobApplicationRepository;
		this.jobApplicationMapper = jobApplicationMapper;
		this.messageService = messageService;
	}

	@Transactional(readOnly = true)
	public ResponseEntity<ApiResponse<PaginatedResponse<JobApplicationResponse>>> getList(
			AdminApplicationFilterRequest filter,
			Pageable pageable) {
		validateStatus(filter.status());

		Page<JobApplicationResponse> page = jobApplicationRepository
				.findAll(JobApplicationSpecification.buildAdmin(filter), pageable)
				.map(jobApplicationMapper::toResponse);

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
}
