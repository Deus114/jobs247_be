package com.recruitment.dto.response.admin.dashboard;

import java.time.Instant;
import java.util.List;

public record AdminDashboardResponse(
		Summary summary,
		List<IndustryCount> jobsByIndustry,
		CompanyStatus companiesByStatus,
		List<MonthCount> jobsByMonth,
		List<RecentJob> recentJobs,
		List<RecentUser> recentUsers) {

	public record Summary(Metric jobs, Metric companies, Metric users, Metric pending) {
	}

	public record Metric(long count, Long changePercent) {
	}

	public record IndustryCount(Long industryId, String industryName, long count) {
	}

	public record CompanyStatus(long pending, long approved, long rejected, long total) {
	}

	public record MonthCount(String month, long count) {
	}

	public record RecentJob(Long id, String title, String companyName, String location, Instant createdAt) {
	}

	public record RecentUser(Long id, String name, String email, String type, Instant createdAt) {
	}
}
