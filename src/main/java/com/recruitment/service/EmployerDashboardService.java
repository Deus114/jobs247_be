package com.recruitment.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.recruitment.dto.request.user.dashboard.EmployerDashboardFilterRequest;
import com.recruitment.dto.response.common.ApiResponse;
import com.recruitment.dto.response.user.dashboard.EmployerDashboardResponse;
import com.recruitment.dto.response.user.dashboard.EmployerDashboardResponse.ApplicationStatus;
import com.recruitment.dto.response.user.dashboard.EmployerDashboardResponse.DayCount;
import com.recruitment.dto.response.user.dashboard.EmployerDashboardResponse.JobApplicationCount;
import com.recruitment.dto.response.user.dashboard.EmployerDashboardResponse.RecentApplication;
import com.recruitment.dto.response.user.dashboard.EmployerDashboardResponse.RecentJob;
import com.recruitment.dto.response.user.dashboard.EmployerDashboardResponse.Summary;
import com.recruitment.entity.Company;
import com.recruitment.entity.Job;
import com.recruitment.entity.JobApplication;
import com.recruitment.entity.User;
import com.recruitment.enums.ApplicationStatusEnum;
import com.recruitment.enums.CompanyMemberRoleEnum;
import com.recruitment.enums.CompanyStatusEnum;
import com.recruitment.enums.JobStatusEnum;
import com.recruitment.enums.UserTypeEnum;
import com.recruitment.exception.AccountInactiveException;
import com.recruitment.exception.BusinessRuleException;
import com.recruitment.exception.InvalidTokenException;
import com.recruitment.repository.CompanyMemberRepository;
import com.recruitment.repository.JobApplicationRepository;
import com.recruitment.repository.JobRepository;
import com.recruitment.repository.UserRepository;
import com.recruitment.security.JwtTokenProvider;

@Service
public class EmployerDashboardService {

	private static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
	private static final int DAYS = 30;
	private static final int NEW_APPLICATION_DAYS = 7;

	private final JobRepository jobRepository;
	private final JobApplicationRepository jobApplicationRepository;
	private final CompanyMemberRepository companyMemberRepository;
	private final UserRepository userRepository;
	private final MessageService messageService;

	public EmployerDashboardService(
			JobRepository jobRepository,
			JobApplicationRepository jobApplicationRepository,
			CompanyMemberRepository companyMemberRepository,
			UserRepository userRepository,
			MessageService messageService) {
		this.jobRepository = jobRepository;
		this.jobApplicationRepository = jobApplicationRepository;
		this.companyMemberRepository = companyMemberRepository;
		this.userRepository = userRepository;
		this.messageService = messageService;
	}

	@Transactional(readOnly = true)
	public ResponseEntity<ApiResponse<EmployerDashboardResponse>> get(
			EmployerDashboardFilterRequest filter,
			Jwt jwt) {
		User employer = requireEmployer(jwt);
		List<Long> companyIds = resolveCompanyIds(employer.getId(), filter == null ? null : filter.companyId());
		LocalDate today = LocalDate.now(ZONE);
		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("employer-dashboard.get.success"),
				companyIds.isEmpty() ? empty(today) : dashboard(companyIds, today)));
	}

	private EmployerDashboardResponse dashboard(List<Long> companyIds, LocalDate today) {
		Instant newSince = today.minusDays(NEW_APPLICATION_DAYS - 1L).atStartOfDay(ZONE).toInstant();
		long submitted = jobApplicationRepository.countByCompanyIdsAndStatus(
				companyIds, ApplicationStatusEnum.SUBMITTED.name());
		long viewed = jobApplicationRepository.countByCompanyIdsAndStatus(
				companyIds, ApplicationStatusEnum.VIEWED.name());
		return new EmployerDashboardResponse(
				new Summary(
						jobRepository.countByCompanyIdIn(companyIds),
						jobRepository.countOpenByCompanyIds(
								companyIds,
								JobStatusEnum.APPROVED.name(),
								CompanyStatusEnum.APPROVED.name(),
								today),
						jobApplicationRepository.countByCompanyIds(companyIds),
						jobApplicationRepository.countByCompanyIdsSince(companyIds, newSince)),
				applicationsByJob(companyIds),
				new ApplicationStatus(submitted, viewed, submitted + viewed),
				applicationsByDay(companyIds, today),
				recentJobs(companyIds, today),
				recentApplications(companyIds));
	}

	private EmployerDashboardResponse empty(LocalDate today) {
		return new EmployerDashboardResponse(
				new Summary(0, 0, 0, 0),
				List.of(),
				new ApplicationStatus(0, 0, 0),
				applicationsByDay(List.of(), today),
				List.of(),
				List.of());
	}

	private List<JobApplicationCount> applicationsByJob(List<Long> companyIds) {
		List<JobApplicationCount> counts = new ArrayList<>();
		for (Object[] row : jobApplicationRepository.countGroupedByCompanyIds(companyIds)) {
			counts.add(new JobApplicationCount(
					((Number) row[0]).longValue(),
					(String) row[1],
					((Number) row[2]).longValue()));
		}
		return counts;
	}

	private List<DayCount> applicationsByDay(List<Long> companyIds, LocalDate today) {
		LocalDate startDay = today.minusDays(DAYS - 1L);
		Map<LocalDate, Long> counts = new HashMap<>();
		if (!companyIds.isEmpty()) {
			Instant start = startDay.atStartOfDay(ZONE).toInstant();
			for (Instant createdAt : jobApplicationRepository.findCreatedAtByCompanyIdsSince(companyIds, start)) {
				LocalDate day = LocalDate.ofInstant(createdAt, ZONE);
				Long current = counts.get(day);
				counts.put(day, current == null ? 1L : current + 1L);
			}
		}
		List<DayCount> days = new ArrayList<>();
		for (int index = 0; index < DAYS; index++) {
			LocalDate day = startDay.plusDays(index);
			days.add(new DayCount(day.toString(), counts.getOrDefault(day, 0L)));
		}
		return days;
	}

	private List<RecentJob> recentJobs(List<Long> companyIds, LocalDate today) {
		List<RecentJob> jobs = new ArrayList<>();
		for (Job job : jobRepository.findTop8ByCompanyIdInOrderByCreatedAtDesc(companyIds)) {
			Company company = job.getCompany();
			jobs.add(new RecentJob(
					job.getId(),
					job.getTitle(),
					company.getName(),
					job.getStatus(),
					isOpen(job, company, today),
					job.getCreatedAt()));
		}
		return jobs;
	}

	private List<RecentApplication> recentApplications(List<Long> companyIds) {
		List<RecentApplication> applications = new ArrayList<>();
		for (JobApplication application : jobApplicationRepository
				.findTop8ByJob_Company_IdInOrderByCreatedAtDesc(companyIds)) {
			Job job = application.getJob();
			applications.add(new RecentApplication(
					application.getId(),
					application.getFullName(),
					job.getId(),
					job.getTitle(),
					application.getStatus(),
					application.getCreatedAt()));
		}
		return applications;
	}

	private boolean isOpen(Job job, Company company, LocalDate today) {
		return JobStatusEnum.APPROVED.name().equals(job.getStatus())
				&& job.isActive()
				&& job.getDeadline() != null
				&& !job.getDeadline().isBefore(today)
				&& CompanyStatusEnum.APPROVED.name().equals(company.getStatus())
				&& company.isActive();
	}

	private List<Long> resolveCompanyIds(Long userId, Long companyId) {
		List<Long> managed = companyMemberRepository.findByUserIdOrderByCreatedAtAsc(userId).stream()
				.filter(member -> isManager(member.getRole()))
				.map(member -> member.getCompany().getId())
				.toList();
		if (companyId == null) {
			return managed;
		}
		if (!managed.contains(companyId)) {
			throw new BusinessRuleException("error.job.member-only");
		}
		return List.of(companyId);
	}

	private boolean isManager(String role) {
		return CompanyMemberRoleEnum.OWNER.name().equals(role)
				|| CompanyMemberRoleEnum.ADMIN.name().equals(role);
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
}
