package com.recruitment.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.recruitment.dto.request.user.job.CreateEmployerJobRequest;
import com.recruitment.dto.request.user.job.EmployerJobFilterRequest;
import com.recruitment.dto.request.user.job.UpdateEmployerJobRequest;
import com.recruitment.dto.response.common.ApiResponse;
import com.recruitment.dto.response.common.PaginatedResponse;
import com.recruitment.dto.response.user.job.EmployerJobResponse;
import com.recruitment.entity.Company;
import com.recruitment.entity.CompanyMember;
import com.recruitment.entity.Job;
import com.recruitment.entity.User;
import com.recruitment.enums.CompanyMemberRoleEnum;
import com.recruitment.enums.CompanyStatusEnum;
import com.recruitment.enums.EmploymentTypeEnum;
import com.recruitment.enums.ExperienceLevelEnum;
import com.recruitment.enums.JobStatusEnum;
import com.recruitment.enums.UserTypeEnum;
import com.recruitment.exception.AccountInactiveException;
import com.recruitment.exception.BusinessRuleException;
import com.recruitment.exception.InvalidTokenException;
import com.recruitment.exception.ResourceNotFoundException;
import com.recruitment.mapper.JobMapper;
import com.recruitment.notification.AdminNotificationService;
import com.recruitment.repository.CompanyMemberRepository;
import com.recruitment.repository.CompanyRepository;
import com.recruitment.repository.JobApplicationRepository;
import com.recruitment.repository.JobRepository;
import com.recruitment.repository.UserRepository;
import com.recruitment.security.JwtTokenProvider;
import com.recruitment.specification.JobSpecification;
import com.recruitment.util.constant.CacheNames;
import com.recruitment.util.helper.AccentHelper;
import com.recruitment.util.helper.ApprovalStatusHelper;
import com.recruitment.util.helper.JobCatalogHelper;
import com.recruitment.util.helper.SlugHelper;
import com.recruitment.util.helper.SoftDeletedPageQuery;

@Service
public class EmployerJobService {

	private final JobRepository jobRepository;
	private final CompanyRepository companyRepository;
	private final CompanyMemberRepository companyMemberRepository;
	private final UserRepository userRepository;
	private final JobApplicationRepository jobApplicationRepository;
	private final JobCatalogHelper jobCatalogHelper;
	private final JobMapper jobMapper;
	private final SoftDeletedPageQuery softDeletedPageQuery;
	private final AdminNotificationService adminNotificationService;
	private final MessageService messageService;

	public EmployerJobService(
			JobRepository jobRepository,
			CompanyRepository companyRepository,
			CompanyMemberRepository companyMemberRepository,
			UserRepository userRepository,
			JobApplicationRepository jobApplicationRepository,
			JobCatalogHelper jobCatalogHelper,
			JobMapper jobMapper,
			SoftDeletedPageQuery softDeletedPageQuery,
			AdminNotificationService adminNotificationService,
			MessageService messageService) {
		this.jobRepository = jobRepository;
		this.companyRepository = companyRepository;
		this.companyMemberRepository = companyMemberRepository;
		this.userRepository = userRepository;
		this.jobApplicationRepository = jobApplicationRepository;
		this.jobCatalogHelper = jobCatalogHelper;
		this.jobMapper = jobMapper;
		this.softDeletedPageQuery = softDeletedPageQuery;
		this.adminNotificationService = adminNotificationService;
		this.messageService = messageService;
	}

	@Transactional(readOnly = true)
	public ResponseEntity<ApiResponse<PaginatedResponse<EmployerJobResponse>>> getList(
			EmployerJobFilterRequest filter,
			Pageable pageable,
			Jwt jwt) {

		User employer = requireEmployer(jwt);
		List<Long> companyIds = resolveScopedCompanyIds(employer.getId(), filter.companyId());
		validateFilters(filter.status(), filter.employmentType(), filter.experienceLevel());
		if (filter.salaryFrom() != null && filter.salaryTo() != null && filter.salaryFrom() > filter.salaryTo()) {
			throw new BusinessRuleException("error.job.salary.range");
		}

		if (companyIds.isEmpty()) {
			return ResponseEntity.ok(ApiResponse.success(
					messageService.get("job.list.success"),
					PaginatedResponse.fromPage(Page.empty(pageable))));
		}

		Page<Job> jobsPage = filter.isDeleted()
				? softDeletedPageQuery.find(
						Job.class,
						"jobs j",
						"j",
						List.of("j.title"),
						AccentHelper.normalizeForSearch(filter.keyword()),
						"""
								AND j.company_id IN (:companyIds)
								AND (:status IS NULL OR j.status = :status)
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
								"""
								+ JobSpecification.NATIVE_SALARY_OVERLAP,
						SoftDeletedPageQuery.params(
								"companyIds", companyIds,
								"status", blankToNullUpper(filter.status()),
								"industryId", filter.industryId(),
								"provinceId", filter.provinceId(),
								"employmentType", blankToNullUpper(filter.employmentType()),
								"experienceLevel", blankToNullUpper(filter.experienceLevel()),
								"active", filter.active(),
								"hot", filter.hot(),
								"salaryFrom", filter.salaryFrom(),
								"salaryTo", filter.salaryTo()),
						"j.created_at DESC, j.id DESC",
						pageable)
				: jobRepository.findAll(JobSpecification.buildEmployer(companyIds, filter), pageable);

		Map<Long, Long> applicationCounts = applicationCounts(jobsPage.getContent());
		Page<EmployerJobResponse> resultPage = jobsPage.map(job -> jobMapper.toEmployerResponse(
				job,
				applicationCounts.getOrDefault(job.getId(), 0L)));

		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("job.list.success"),
				PaginatedResponse.fromPage(resultPage)));
	}

	@Transactional(readOnly = true)
	public ResponseEntity<ApiResponse<EmployerJobResponse>> getById(Long id, Jwt jwt) {
		User employer = requireEmployer(jwt);
		Job job = requireAccessibleJob(id, employer.getId());
		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("common.success"),
				jobMapper.toEmployerResponse(job, jobApplicationRepository.countByJobId(job.getId()))));
	}

	@Transactional
	public ResponseEntity<ApiResponse<EmployerJobResponse>> create(
			CreateEmployerJobRequest request,
			Jwt jwt) {

		User employer = requireEmployer(jwt);
		requireMember(request.companyId(), employer.getId());
		Company company = findActiveCompany(request.companyId());
		if (!CompanyStatusEnum.APPROVED.name().equals(company.getStatus())) {
			throw new BusinessRuleException("error.job.company-not-approved");
		}

		validateSalary(request.salaryMin(), request.salaryMax());

		Job job = new Job();
		job.setCompany(company);
		job.setCreatedByUser(employer);
		job.setTitle(request.title().trim());
		job.setSlug(SlugHelper.from(request.title()));
		job.setIndustries(jobCatalogHelper.requireActiveIndustries(request.industryIds()));
		job.setEducationLevels(jobCatalogHelper.requireActiveEducationLevels(request.educationLevelIds()));
		job.setProvinces(jobCatalogHelper.requireActiveProvinces(request.provinceIds()));
		job.setEmploymentTypes(jobCatalogHelper.requireEmploymentTypes(request.employmentTypes()));
		job.setExperienceLevels(jobCatalogHelper.resolveExperienceLevels(request.experienceLevels()));
		job.setExperienceYears(request.experienceYears());
		job.setSalaryMin(request.salaryMin());
		job.setSalaryMax(request.salaryMax());
		job.setSalaryNegotiable(Boolean.TRUE.equals(request.salaryNegotiable()));
		job.setDeadline(request.deadline());
		job.setWorkLocation(trimToNull(request.workLocation()));
		job.setWorkingTime(trimToNull(request.workingTime()));
		job.setDescription(request.description().trim());
		job.setRequirements(request.requirements().trim());
		job.setBenefits(request.benefits().trim());
		job.setApplicantQuestion(trimToNull(request.applicantQuestion()));
		job.setHot(false);
		job.setStatus(JobStatusEnum.PENDING.name());
		job.setCreatedBy(employer.getEmail());
		job.setUpdatedBy(employer.getEmail());

		Job saved = jobRepository.save(job);
		adminNotificationService.notifyJobPending(saved, employer.getEmail());
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(
				HttpStatus.CREATED.value(),
				messageService.get("job.create.success"),
				jobMapper.toEmployerResponse(saved, 0L)));
	}

	@Transactional
	@CacheEvict(cacheNames = CacheNames.HOT_JOBS, allEntries = true)
	public ResponseEntity<ApiResponse<EmployerJobResponse>> update(
			Long id,
			UpdateEmployerJobRequest request,
			Jwt jwt) {

		User employer = requireEmployer(jwt);
		Job job = requireAccessibleJob(id, employer.getId());
		String previousStatus = job.getStatus();
		boolean contentChanged = false;

		if (StringUtils.hasText(request.title())) {
			job.setTitle(request.title().trim());
			job.setSlug(SlugHelper.from(request.title()));
			contentChanged = true;
		}
		if (request.industryIds() != null) {
			if (request.industryIds().isEmpty()) {
				throw new BusinessRuleException("error.job.industries.required");
			}
			job.setIndustries(jobCatalogHelper.requireActiveIndustries(request.industryIds()));
			contentChanged = true;
		}
		if (request.educationLevelIds() != null) {
			job.setEducationLevels(jobCatalogHelper.requireActiveEducationLevels(request.educationLevelIds()));
			contentChanged = true;
		}
		if (request.provinceIds() != null) {
			if (request.provinceIds().isEmpty()) {
				throw new BusinessRuleException("error.job.provinces.required");
			}
			job.setProvinces(jobCatalogHelper.requireActiveProvinces(request.provinceIds()));
			contentChanged = true;
		}
		if (request.employmentTypes() != null) {
			job.setEmploymentTypes(jobCatalogHelper.requireEmploymentTypes(request.employmentTypes()));
			contentChanged = true;
		}
		if (request.experienceLevels() != null) {
			job.setExperienceLevels(jobCatalogHelper.resolveExperienceLevels(request.experienceLevels()));
			contentChanged = true;
		}
		if (request.experienceYears() != null) {
			job.setExperienceYears(request.experienceYears());
			contentChanged = true;
		}
		if (request.salaryMin() != null || request.salaryMax() != null) {
			Integer min = request.salaryMin() != null ? request.salaryMin() : job.getSalaryMin();
			Integer max = request.salaryMax() != null ? request.salaryMax() : job.getSalaryMax();
			validateSalary(min, max);
			if (request.salaryMin() != null) {
				job.setSalaryMin(request.salaryMin());
			}
			if (request.salaryMax() != null) {
				job.setSalaryMax(request.salaryMax());
			}
			contentChanged = true;
		}
		if (request.salaryNegotiable() != null) {
			job.setSalaryNegotiable(request.salaryNegotiable());
			contentChanged = true;
		}
		if (request.deadline() != null) {
			job.setDeadline(request.deadline());
			contentChanged = true;
		}
		if (request.workLocation() != null) {
			job.setWorkLocation(trimToNull(request.workLocation()));
			contentChanged = true;
		}
		if (request.workingTime() != null) {
			job.setWorkingTime(trimToNull(request.workingTime()));
			contentChanged = true;
		}
		if (StringUtils.hasText(request.description())) {
			job.setDescription(request.description().trim());
			contentChanged = true;
		}
		if (StringUtils.hasText(request.requirements())) {
			job.setRequirements(request.requirements().trim());
			contentChanged = true;
		}
		if (StringUtils.hasText(request.benefits())) {
			job.setBenefits(request.benefits().trim());
			contentChanged = true;
		}
		if (request.applicantQuestion() != null) {
			job.setApplicantQuestion(trimToNull(request.applicantQuestion()));
			contentChanged = true;
		}

		if (contentChanged
				&& (JobStatusEnum.APPROVED.name().equals(job.getStatus())
						|| JobStatusEnum.REJECTED.name().equals(job.getStatus()))) {
			var fields = ApprovalStatusHelper.apply(
					JobStatusEnum.PENDING.name(),
					null,
					"error.job.status.invalid");
			job.setStatus(fields.status());
			job.setRejectionReason(fields.rejectionReason());
			job.setRejectedAt(fields.rejectedAt());
			job.setApprovedAt(fields.approvedAt());
		}

		job.setUpdatedBy(employer.getEmail());
		Job saved = jobRepository.save(job);
		if (JobStatusEnum.PENDING.name().equals(saved.getStatus())
				&& !JobStatusEnum.PENDING.name().equals(previousStatus)) {
			adminNotificationService.notifyJobPending(saved, employer.getEmail());
		}
		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("job.update.success"),
				jobMapper.toEmployerResponse(saved, jobApplicationRepository.countByJobId(saved.getId()))));
	}

	@Transactional
	@CacheEvict(cacheNames = CacheNames.HOT_JOBS, allEntries = true)
	public ResponseEntity<ApiResponse<Void>> softDelete(Long id, Jwt jwt) {
		User employer = requireEmployer(jwt);
		Job job = requireAccessibleJob(id, employer.getId());
		job.setDeletedBy(employer.getEmail());
		jobRepository.save(job);
		jobRepository.delete(job);
		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("job.soft-delete.success"),
				null));
	}

	@Transactional
	@CacheEvict(cacheNames = CacheNames.HOT_JOBS, allEntries = true)
	public ResponseEntity<ApiResponse<Void>> restore(Long id, Jwt jwt) {
		User employer = requireEmployer(jwt);
		Job softDeleted = jobRepository.findSoftDeletedById(id)
				.orElseThrow(() -> new ResourceNotFoundException("error.resource.job", "id", id));
		requireMember(softDeleted.getCompany().getId(), employer.getId());
		int restored = jobRepository.restoreById(id);
		if (restored == 0) {
			throw new ResourceNotFoundException("error.resource.job", "id", id);
		}
		Job job = requireAccessibleJob(id, employer.getId());
		job.setUpdatedBy(employer.getEmail());
		jobRepository.save(job);
		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("job.restore.success"),
				null));
	}

	private User requireEmployer(Jwt jwt) {
		if (jwt == null
				|| !JwtTokenProvider.USER_ACCOUNT.equals(jwt.getClaimAsString("account"))
				|| !JwtTokenProvider.USER_ACCESS_TYPE.equals(jwt.getClaimAsString("type"))) {
			throw new InvalidTokenException("error.unauthorized.invalid-token");
		}
		User user = userRepository.findByEmail(jwt.getSubject())
				.orElseThrow(() -> new InvalidTokenException("error.unauthorized.invalid-token"));
		if (!user.isActive()) {
			throw new AccountInactiveException("error.auth.account.inactive");
		}
		if (!UserTypeEnum.EMPLOYER.name().equals(user.getType())) {
			throw new BusinessRuleException("error.company.employer-only");
		}
		return user;
	}

	private List<Long> managedCompanyIds(Long userId) {
		return companyMemberRepository.findByUserIdOrderByCreatedAtAsc(userId).stream()
				.filter(member -> {
					String role = member.getRole();
					return CompanyMemberRoleEnum.OWNER.name().equals(role)
							|| CompanyMemberRoleEnum.ADMIN.name().equals(role);
				})
				.map(member -> member.getCompany().getId())
				.toList();
	}

	private List<Long> resolveScopedCompanyIds(Long userId, Long companyIdFilter) {
		List<Long> managed = managedCompanyIds(userId);
		if (companyIdFilter == null) {
			return managed;
		}
		requireMember(companyIdFilter, userId);
		return List.of(companyIdFilter);
	}

	private Job requireAccessibleJob(Long jobId, Long userId) {
		Job job = jobRepository.findById(jobId)
				.orElseThrow(() -> new ResourceNotFoundException("error.resource.job", "id", jobId));
		requireMember(job.getCompany().getId(), userId);
		return job;
	}

	private void requireMember(Long companyId, Long userId) {
		CompanyMember member = companyMemberRepository.findByCompanyIdAndUserId(companyId, userId)
				.orElseThrow(() -> new BusinessRuleException("error.job.member-only"));
		String role = member.getRole();
		if (!CompanyMemberRoleEnum.OWNER.name().equals(role)
				&& !CompanyMemberRoleEnum.ADMIN.name().equals(role)) {
			throw new BusinessRuleException("error.job.member-only");
		}
	}

	private Company findActiveCompany(Long companyId) {
		return companyRepository.findById(companyId)
				.orElseThrow(() -> new ResourceNotFoundException("error.resource.company", "id", companyId));
	}

	private void validateFilters(String status, String employmentType, String experienceLevel) {
		if (StringUtils.hasText(status)) {
			JobStatusEnum.fromValue(status)
					.orElseThrow(() -> new BusinessRuleException("error.job.status.invalid", status));
		}
		if (StringUtils.hasText(employmentType)) {
			EmploymentTypeEnum.fromValue(employmentType)
					.orElseThrow(() -> new BusinessRuleException(
							"error.job.employment-type.invalid", employmentType));
		}
		if (StringUtils.hasText(experienceLevel)) {
			ExperienceLevelEnum.fromValue(experienceLevel)
					.orElseThrow(() -> new BusinessRuleException(
							"error.job.experience-level.invalid", experienceLevel));
		}
	}

	private void validateSalary(Integer min, Integer max) {
		if (min != null && max != null && min > max) {
			throw new BusinessRuleException("error.job.salary.range");
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

	private String trimToNull(String value) {
		if (!StringUtils.hasText(value)) {
			return null;
		}
		return value.trim();
	}
}
