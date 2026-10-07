package com.recruitment.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.recruitment.dto.request.admin.job.AdminJobFilterRequest;
import com.recruitment.dto.request.admin.job.UpdateAdminJobRequest;
import com.recruitment.dto.response.admin.job.AdminJobResponse;
import com.recruitment.dto.response.common.ApiResponse;
import com.recruitment.dto.response.common.PaginatedResponse;
import com.recruitment.entity.Job;
import com.recruitment.enums.EmploymentTypeEnum;
import com.recruitment.enums.ExperienceLevelEnum;
import com.recruitment.enums.JobStatusEnum;
import com.recruitment.exception.BusinessRuleException;
import com.recruitment.exception.ResourceNotFoundException;
import com.recruitment.mapper.JobMapper;
import com.recruitment.repository.JobApplicationRepository;
import com.recruitment.repository.JobRepository;
import com.recruitment.specification.JobSpecification;
import com.recruitment.util.constant.CacheNames;
import com.recruitment.util.helper.AccentHelper;
import com.recruitment.util.helper.ApprovalStatusHelper;
import com.recruitment.util.helper.SoftDeletedPageQuery;

@Service
public class AdminJobService {

	private final JobRepository jobRepository;
	private final JobApplicationRepository jobApplicationRepository;
	private final JobMapper jobMapper;
	private final SoftDeletedPageQuery softDeletedPageQuery;
	private final MessageService messageService;

	public AdminJobService(
			JobRepository jobRepository,
			JobApplicationRepository jobApplicationRepository,
			JobMapper jobMapper,
			SoftDeletedPageQuery softDeletedPageQuery,
			MessageService messageService) {
		this.jobRepository = jobRepository;
		this.jobApplicationRepository = jobApplicationRepository;
		this.jobMapper = jobMapper;
		this.softDeletedPageQuery = softDeletedPageQuery;
		this.messageService = messageService;
	}

	@Transactional(readOnly = true)
	public ResponseEntity<ApiResponse<PaginatedResponse<AdminJobResponse>>> getList(
			AdminJobFilterRequest filter,
			Pageable pageable) {

		validateFilters(filter);

		Page<Job> jobsPage = filter.isDeleted()
				? softDeletedPageQuery.find(
						Job.class,
						"jobs j",
						"j",
						List.of("j.title"),
						AccentHelper.normalizeForSearch(filter.keyword()),
						"""
								AND (:status IS NULL OR j.status = :status)
								AND (:companyId IS NULL OR j.company_id = :companyId)
								AND (:industryId IS NULL OR EXISTS (
									SELECT 1 FROM job_industries ji
									WHERE ji.job_id = j.id AND ji.industry_id = :industryId))
								AND (:provinceId IS NULL OR EXISTS (
									SELECT 1 FROM job_provinces jp
									WHERE jp.job_id = j.id AND jp.province_id = :provinceId))
								AND (:employmentType IS NULL OR EXISTS (
									SELECT 1 FROM job_employment_types jet
									WHERE jet.job_id = j.id AND jet.employment_type = :employmentType))
								AND (:experienceLevel IS NULL OR EXISTS (
									SELECT 1 FROM job_experience_levels jel
									WHERE jel.job_id = j.id AND jel.experience_level = :experienceLevel))
								AND (:active IS NULL OR j.active = :active)
								AND (:hot IS NULL OR j.is_hot = :hot)
								""",
						SoftDeletedPageQuery.params(
								"status", blankToNullUpper(filter.status()),
								"companyId", filter.companyId(),
								"industryId", filter.industryId(),
								"provinceId", filter.provinceId(),
								"employmentType", blankToNullUpper(filter.employmentType()),
								"experienceLevel", blankToNullUpper(filter.experienceLevel()),
								"active", filter.active(),
								"hot", filter.hot()),
						"j.created_at DESC, j.id DESC",
						pageable)
				: jobRepository.findAll(JobSpecification.buildAdmin(filter), pageable);

		Map<Long, Long> applicationCounts = applicationCounts(jobsPage.getContent());
		Page<AdminJobResponse> resultPage = jobsPage.map(job -> jobMapper.toAdminResponse(
				job,
				applicationCounts.getOrDefault(job.getId(), 0L)));

		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("admin-job.list.success"),
				PaginatedResponse.fromPage(resultPage)));
	}

	@Transactional(readOnly = true)
	public ResponseEntity<ApiResponse<AdminJobResponse>> getById(Long id) {
		Job job = findActiveById(id);
		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("common.success"),
				jobMapper.toAdminResponse(job, jobApplicationRepository.countByJobId(job.getId()))));
	}

	@Transactional
	@CacheEvict(cacheNames = CacheNames.HOT_JOBS, allEntries = true)
	public ResponseEntity<ApiResponse<AdminJobResponse>> update(
			Long id,
			UpdateAdminJobRequest request,
			String currentUsername) {

		if (request.active() == null
				&& !StringUtils.hasText(request.status())
				&& request.hot() == null) {
			throw new BusinessRuleException("error.admin-job.update.empty");
		}

		Job job = findActiveById(id);
		if (request.active() != null) {
			job.setActive(request.active());
		}
		if (request.hot() != null) {
			job.setHot(request.hot());
		}
		if (StringUtils.hasText(request.status())) {
			var fields = ApprovalStatusHelper.apply(
					request.status(),
					request.rejectionReason(),
					"error.job.status.invalid");
			job.setStatus(fields.status());
			job.setRejectionReason(fields.rejectionReason());
			job.setRejectedAt(fields.rejectedAt());
			job.setApprovedAt(fields.approvedAt());
		}
		job.setUpdatedBy(currentUsername);

		Job saved = jobRepository.save(job);
		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("admin-job.update.success"),
				jobMapper.toAdminResponse(saved, jobApplicationRepository.countByJobId(saved.getId()))));
	}

	@Transactional
	@CacheEvict(cacheNames = CacheNames.HOT_JOBS, allEntries = true)
	public ResponseEntity<ApiResponse<Void>> softDelete(Long id, String currentUsername) {
		Job job = findActiveById(id);
		job.setDeletedBy(currentUsername);
		jobRepository.save(job);
		jobRepository.delete(job);
		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("admin-job.soft-delete.success"),
				null));
	}

	@Transactional
	@CacheEvict(cacheNames = CacheNames.HOT_JOBS, allEntries = true)
	public ResponseEntity<ApiResponse<Void>> restore(Long id, String currentUsername) {
		int restored = jobRepository.restoreById(id);
		if (restored == 0) {
			throw new ResourceNotFoundException("error.resource.job", "id", id);
		}
		Job job = findActiveById(id);
		job.setUpdatedBy(currentUsername);
		jobRepository.save(job);
		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("admin-job.restore.success"),
				null));
	}

	private Job findActiveById(Long id) {
		return jobRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("error.resource.job", "id", id));
	}

	private void validateFilters(AdminJobFilterRequest filter) {
		if (StringUtils.hasText(filter.status())) {
			JobStatusEnum.fromValue(filter.status())
					.orElseThrow(() -> new BusinessRuleException("error.job.status.invalid", filter.status()));
		}
		if (StringUtils.hasText(filter.employmentType())) {
			EmploymentTypeEnum.fromValue(filter.employmentType())
					.orElseThrow(() -> new BusinessRuleException(
							"error.job.employment-type.invalid", filter.employmentType()));
		}
		if (StringUtils.hasText(filter.experienceLevel())) {
			ExperienceLevelEnum.fromValue(filter.experienceLevel())
					.orElseThrow(() -> new BusinessRuleException(
							"error.job.experience-level.invalid", filter.experienceLevel()));
		}
	}

	private Map<Long, Long> applicationCounts(List<Job> jobs) {
		ArrayList<Long> jobIds = new ArrayList<>(jobs.size());
		for (Job job : jobs) {
			jobIds.add(job.getId());
		}
		return jobApplicationRepository.countMapByJobIds(jobIds);
	}

	private String blankToNullUpper(String value) {
		if (!StringUtils.hasText(value)) {
			return null;
		}
		return value.trim().toUpperCase();
	}
}
