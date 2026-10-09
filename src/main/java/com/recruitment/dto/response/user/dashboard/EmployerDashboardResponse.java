package com.recruitment.dto.response.user.dashboard;

import java.time.Instant;
import java.util.List;

public record EmployerDashboardResponse(
		Summary summary,
		List<JobApplicationCount> applicationsByJob,
		ApplicationStatus applicationsByStatus,
		List<DayCount> applicationsByDay,
		List<RecentJob> recentJobs,
		List<RecentApplication> recentApplications) {

	public record Summary(
			long jobCount,
			long openJobCount,
			long applicationCount,
			long newApplicationCount) {
	}

	public record JobApplicationCount(Long jobId, String title, long count) {
	}

	public record ApplicationStatus(long submitted, long viewed, long total) {
	}

	public record DayCount(String date, long count) {
	}

	public record RecentJob(
			Long id,
			String title,
			String companyName,
			String status,
			boolean open,
			Instant createdAt) {
	}

	public record RecentApplication(
			Long id,
			String applicantName,
			Long jobId,
			String jobTitle,
			String status,
			Instant createdAt) {
	}
}
