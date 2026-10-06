package com.recruitment.service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.recruitment.dto.request.user.application.CreateJobApplicationRequest;
import com.recruitment.dto.request.user.application.EmployerApplicationFilterRequest;
import com.recruitment.dto.request.user.application.JobSeekerApplicationFilterRequest;
import com.recruitment.dto.response.common.ApiResponse;
import com.recruitment.dto.response.common.PaginatedResponse;
import com.recruitment.dto.response.common.UploadedFileResponse;
import com.recruitment.dto.response.user.application.JobApplicationResponse;
import com.recruitment.entity.Company;
import com.recruitment.entity.CompanyMember;
import com.recruitment.entity.Job;
import com.recruitment.entity.JobApplication;
import com.recruitment.entity.User;
import com.recruitment.enums.ApplicationStatusEnum;
import com.recruitment.enums.CompanyMemberRoleEnum;
import com.recruitment.enums.CompanyStatusEnum;
import com.recruitment.enums.EmailType;
import com.recruitment.enums.JobStatusEnum;
import com.recruitment.enums.UploadFolderType;
import com.recruitment.enums.UserTypeEnum;
import com.recruitment.exception.AccountInactiveException;
import com.recruitment.exception.BusinessRuleException;
import com.recruitment.exception.InvalidTokenException;
import com.recruitment.exception.ResourceNotFoundException;
import com.recruitment.mail.EmailPublisher;
import com.recruitment.mapper.JobApplicationMapper;
import com.recruitment.repository.CompanyMemberRepository;
import com.recruitment.repository.JobApplicationRepository;
import com.recruitment.repository.JobRepository;
import com.recruitment.repository.UserRepository;
import com.recruitment.security.JwtTokenProvider;
import com.recruitment.specification.JobApplicationSpecification;

@Service
public class JobApplicationService {

	private final JobApplicationRepository jobApplicationRepository;
	private final JobRepository jobRepository;
	private final UserRepository userRepository;
	private final CompanyMemberRepository companyMemberRepository;
	private final FileStorageService fileStorageService;
	private final JobApplicationMapper jobApplicationMapper;
	private final EmailPublisher emailPublisher;
	private final MessageService messageService;

	public JobApplicationService(
			JobApplicationRepository jobApplicationRepository,
			JobRepository jobRepository,
			UserRepository userRepository,
			CompanyMemberRepository companyMemberRepository,
			FileStorageService fileStorageService,
			JobApplicationMapper jobApplicationMapper,
			EmailPublisher emailPublisher,
			MessageService messageService) {
		this.jobApplicationRepository = jobApplicationRepository;
		this.jobRepository = jobRepository;
		this.userRepository = userRepository;
		this.companyMemberRepository = companyMemberRepository;
		this.fileStorageService = fileStorageService;
		this.jobApplicationMapper = jobApplicationMapper;
		this.emailPublisher = emailPublisher;
		this.messageService = messageService;
	}

	@Transactional
	public ResponseEntity<ApiResponse<JobApplicationResponse>> apply(
			Long jobId,
			CreateJobApplicationRequest request,
			Jwt jwt) {
		User seeker = requireJobSeeker(jwt);
		Job job = requireOpenJob(jobId);
		if (jobApplicationRepository.countByJobIdAndUserIdIncludingDeleted(job.getId(), seeker.getId()) > 0) {
			throw new BusinessRuleException("error.application.duplicate");
		}

		UploadedFileResponse uploaded = fileStorageService.uploadDocument(request.cvFile(), UploadFolderType.CV);

		JobApplication application = new JobApplication();
		application.setJob(job);
		application.setUser(seeker);
		application.setFullName(request.fullName().trim());
		application.setEmail(request.email().trim().toLowerCase(Locale.ROOT));
		application.setPhone(trimToNull(request.phone()));
		application.setCoverLetter(trimToNull(request.coverLetter()));
		application.setCvUrl(uploaded.url());
		application.setStatus(ApplicationStatusEnum.SUBMITTED.name());
		application.setCreatedBy(seeker.getEmail());
		application.setUpdatedBy(seeker.getEmail());
		JobApplication saved = jobApplicationRepository.save(application);

		publishApplicationEmails(saved);

		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(
				HttpStatus.CREATED.value(),
				messageService.get("application.create.success"),
				jobApplicationMapper.toResponse(saved)));
	}

	@Transactional(readOnly = true)
	public ResponseEntity<ApiResponse<PaginatedResponse<JobApplicationResponse>>> getMyApplications(
			JobSeekerApplicationFilterRequest filter,
			Pageable pageable,
			Jwt jwt) {
		User seeker = requireJobSeeker(jwt);
		validateStatus(filter.status());
		Page<JobApplicationResponse> page = jobApplicationRepository
				.findAll(JobApplicationSpecification.buildJobSeeker(seeker.getId(), filter), pageable)
				.map(jobApplicationMapper::toResponse);
		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("application.list.success"),
				PaginatedResponse.fromPage(page)));
	}

	@Transactional(readOnly = true)
	public ResponseEntity<ApiResponse<JobApplicationResponse>> getMyApplication(Long id, Jwt jwt) {
		User seeker = requireJobSeeker(jwt);
		JobApplication application = jobApplicationRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("error.resource.application", "id", id));
		if (!application.getUser().getId().equals(seeker.getId())) {
			throw new ResourceNotFoundException("error.resource.application", "id", id);
		}
		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("common.success"),
				jobApplicationMapper.toResponse(application)));
	}

	@Transactional(readOnly = true)
	public ResponseEntity<ApiResponse<PaginatedResponse<JobApplicationResponse>>> getEmployerList(
			EmployerApplicationFilterRequest filter,
			Pageable pageable,
			Jwt jwt) {
		User employer = requireEmployer(jwt);
		List<Long> companyIds = resolveScopedCompanyIds(employer.getId(), filter.companyId());
		validateStatus(filter.status());

		if (companyIds.isEmpty()) {
			return ResponseEntity.ok(ApiResponse.success(
					messageService.get("application.list.success"),
					PaginatedResponse.fromPage(Page.empty(pageable))));
		}

		Page<JobApplicationResponse> page = jobApplicationRepository
				.findAll(JobApplicationSpecification.buildEmployer(companyIds, filter), pageable)
				.map(jobApplicationMapper::toResponse);

		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("application.list.success"),
				PaginatedResponse.fromPage(page)));
	}

	@Transactional
	public ResponseEntity<ApiResponse<JobApplicationResponse>> getEmployerById(Long id, Jwt jwt) {
		User employer = requireEmployer(jwt);
		JobApplication application = jobApplicationRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("error.resource.application", "id", id));
		requireMember(application.getJob().getCompany().getId(), employer.getId());

		if (ApplicationStatusEnum.SUBMITTED.name().equals(application.getStatus())) {
			application.setStatus(ApplicationStatusEnum.VIEWED.name());
			application.setViewedAt(Instant.now());
			application.setViewedByUser(employer);
			application.setUpdatedBy(employer.getEmail());
			application = jobApplicationRepository.save(application);
		}

		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("common.success"),
				jobApplicationMapper.toResponse(application)));
	}

	private void publishApplicationEmails(JobApplication application) {
		String locale = LocaleContextHolder.getLocale().toLanguageTag();
		Job job = application.getJob();
		User applicant = application.getUser();
		Map<String, String> variables = Map.of(
				"applicantName", application.getFullName(),
				"jobTitle", job.getTitle(),
				"companyName", job.getCompany().getName());

		emailPublisher.publish(
				EmailType.APPLICATION_SUBMITTED.name(),
				applicant.getEmail(),
				locale,
				variables);

		Map<String, String> memberEmails = new LinkedHashMap<>();
		for (CompanyMember member : companyMemberRepository.findByCompanyIdOrderByCreatedAtAsc(
				job.getCompany().getId())) {
			User user = member.getUser();
			if (user != null && user.isActive() && StringUtils.hasText(user.getEmail())) {
				memberEmails.putIfAbsent(user.getEmail().trim().toLowerCase(Locale.ROOT), user.getEmail());
			}
		}
		for (String email : memberEmails.values()) {
			emailPublisher.publish(
					EmailType.APPLICATION_RECEIVED.name(),
					email,
					locale,
					variables);
		}
	}

	private Job requireOpenJob(Long jobId) {
		Job job = jobRepository.findById(jobId)
				.orElseThrow(() -> new ResourceNotFoundException("error.resource.job", "id", jobId));
		Company company = job.getCompany();
		if (!JobStatusEnum.APPROVED.name().equals(job.getStatus())
				|| !job.isActive()
				|| job.getDeadline() == null
				|| job.getDeadline().isBefore(LocalDate.now())
				|| !CompanyStatusEnum.APPROVED.name().equals(company.getStatus())
				|| !company.isActive()) {
			throw new BusinessRuleException("error.application.job-closed");
		}
		return job;
	}

	private User requireJobSeeker(Jwt jwt) {
		User user = requireUser(jwt);
		if (!UserTypeEnum.JOB_SEEKER.name().equals(user.getType())) {
			throw new BusinessRuleException("error.application.job-seeker-only");
		}
		return user;
	}

	private User requireEmployer(Jwt jwt) {
		User user = requireUser(jwt);
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

	private User requireUser(Jwt jwt) {
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
		return user;
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

	private void validateStatus(String status) {
		if (StringUtils.hasText(status)) {
			ApplicationStatusEnum.fromValue(status)
					.orElseThrow(() -> new BusinessRuleException("error.application.status.invalid", status));
		}
	}

	private String trimToNull(String value) {
		if (!StringUtils.hasText(value)) {
			return null;
		}
		return value.trim();
	}
}
