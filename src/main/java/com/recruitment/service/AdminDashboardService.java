package com.recruitment.service;

import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.recruitment.dto.response.admin.dashboard.AdminDashboardResponse;
import com.recruitment.dto.response.admin.dashboard.AdminDashboardResponse.CompanyStatus;
import com.recruitment.dto.response.admin.dashboard.AdminDashboardResponse.IndustryCount;
import com.recruitment.dto.response.admin.dashboard.AdminDashboardResponse.Metric;
import com.recruitment.dto.response.admin.dashboard.AdminDashboardResponse.MonthCount;
import com.recruitment.dto.response.admin.dashboard.AdminDashboardResponse.RecentJob;
import com.recruitment.dto.response.admin.dashboard.AdminDashboardResponse.RecentUser;
import com.recruitment.dto.response.admin.dashboard.AdminDashboardResponse.Summary;
import com.recruitment.dto.response.common.ApiResponse;
import com.recruitment.entity.Job;
import com.recruitment.entity.Province;
import com.recruitment.entity.User;
import com.recruitment.enums.CompanyStatusEnum;
import com.recruitment.enums.JobStatusEnum;
import com.recruitment.repository.CompanyRepository;
import com.recruitment.repository.JobRepository;
import com.recruitment.repository.UserRepository;
import com.recruitment.util.helper.LocaleTextHelper;

@Service
public class AdminDashboardService {

	private static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
	private static final int MONTHS = 6;

	private final JobRepository jobRepository;
	private final CompanyRepository companyRepository;
	private final UserRepository userRepository;
	private final MessageService messageService;

	public AdminDashboardService(
			JobRepository jobRepository,
			CompanyRepository companyRepository,
			UserRepository userRepository,
			MessageService messageService) {
		this.jobRepository = jobRepository;
		this.companyRepository = companyRepository;
		this.userRepository = userRepository;
		this.messageService = messageService;
	}

	@Transactional(readOnly = true)
	public ResponseEntity<ApiResponse<AdminDashboardResponse>> get() {
		YearMonth currentMonth = YearMonth.now(ZONE);
		Instant currentStart = monthStart(currentMonth);

		long jobCount = jobRepository.count();
		long companyCount = companyRepository.count();
		long userCount = userRepository.count();
		long pendingJobs = jobRepository.countByStatus(JobStatusEnum.PENDING.name());
		long pendingCompanies = companyRepository.countByStatus(CompanyStatusEnum.PENDING.name());
		long pendingCount = pendingJobs + pendingCompanies;

		Summary summary = new Summary(
				new Metric(
						jobCount,
						changePercent(jobCount, jobRepository.countByCreatedAtLessThan(currentStart))),
				new Metric(
						companyCount,
						changePercent(companyCount, companyRepository.countByCreatedAtLessThan(currentStart))),
				new Metric(
						userCount,
						changePercent(userCount, userRepository.countByCreatedAtLessThan(currentStart))),
				new Metric(
						pendingCount,
						changePercent(pendingCount, pendingBefore(currentStart))));

		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("admin-dashboard.get.success"),
				new AdminDashboardResponse(
						summary,
						jobsByIndustry(),
						companiesByStatus(companyCount),
						jobsByMonth(currentMonth),
						recentJobs(),
						recentUsers())));
	}

	private long pendingBefore(Instant end) {
		return jobRepository.countByStatusAndCreatedAtLessThan(JobStatusEnum.PENDING.name(), end)
				+ companyRepository.countByStatusAndCreatedAtLessThan(CompanyStatusEnum.PENDING.name(), end);
	}

	private List<IndustryCount> jobsByIndustry() {
		List<IndustryCount> counts = new ArrayList<>();
		for (Object[] row : jobRepository.countByIndustry()) {
			counts.add(new IndustryCount(
					((Number) row[0]).longValue(),
					LocaleTextHelper.pick((String) row[1], (String) row[2]),
					((Number) row[3]).longValue()));
		}
		return counts;
	}

	private CompanyStatus companiesByStatus(long total) {
		return new CompanyStatus(
				companyRepository.countByStatus(CompanyStatusEnum.PENDING.name()),
				companyRepository.countByStatus(CompanyStatusEnum.APPROVED.name()),
				companyRepository.countByStatus(CompanyStatusEnum.REJECTED.name()),
				total);
	}

	private List<MonthCount> jobsByMonth(YearMonth currentMonth) {
		YearMonth startMonth = currentMonth.minusMonths(MONTHS - 1L);
		List<MonthCount> months = new ArrayList<>();
		for (int index = 0; index < MONTHS; index++) {
			YearMonth month = startMonth.plusMonths(index);
			Instant start = monthStart(month);
			Instant end = monthStart(month.plusMonths(1));
			months.add(new MonthCount(
					month.toString(),
					jobRepository.countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(start, end)));
		}
		return months;
	}

	private List<RecentJob> recentJobs() {
		List<RecentJob> jobs = new ArrayList<>();
		for (Job job : jobRepository.findTop8ByOrderByCreatedAtDesc()) {
			jobs.add(new RecentJob(
					job.getId(),
					job.getTitle(),
					job.getCompany().getName(),
					location(job),
					job.getCreatedAt()));
		}
		return jobs;
	}

	private List<RecentUser> recentUsers() {
		List<RecentUser> users = new ArrayList<>();
		for (User user : userRepository.findTop8ByOrderByCreatedAtDesc()) {
			users.add(new RecentUser(
					user.getId(),
					user.getName(),
					user.getEmail(),
					user.getType(),
					user.getCreatedAt()));
		}
		return users;
	}

	private String location(Job job) {
		List<Province> provinces = job.getProvinces();
		if (provinces != null && !provinces.isEmpty()) {
			return provinces.get(0).getName();
		}
		return job.getWorkLocation();
	}

	private Instant monthStart(YearMonth month) {
		return month.atDay(1).atStartOfDay(ZONE).toInstant();
	}

	private Long changePercent(long current, long previous) {
		if (previous == 0) {
			return current == 0 ? 0L : null;
		}
		return Math.round((current - previous) * 100.0 / previous);
	}
}
