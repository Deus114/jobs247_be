package com.recruitment.mapper;

import org.springframework.stereotype.Component;

import com.recruitment.dto.response.user.application.JobApplicationResponse;
import com.recruitment.dto.response.user.application.SavedJobResponse;
import com.recruitment.entity.Company;
import com.recruitment.entity.Job;
import com.recruitment.entity.JobApplication;
import com.recruitment.entity.SavedJob;
import com.recruitment.entity.User;

@Component
public class JobApplicationMapper {

	private final JobMapper jobMapper;

	public JobApplicationMapper(JobMapper jobMapper) {
		this.jobMapper = jobMapper;
	}

	public JobApplicationResponse toResponse(JobApplication application) {
		Job job = application.getJob();
		Company company = job.getCompany();
		User applicant = application.getUser();
		User viewedBy = application.getViewedByUser();
		return new JobApplicationResponse(
				application.getId(),
				job.getId(),
				job.getTitle(),
				company.getId(),
				company.getName(),
				applicant.getId(),
				application.getFullName(),
				application.getEmail(),
				application.getPhone(),
				application.getCvUrl(),
				application.getCoverLetter(),
				application.getStatus(),
				application.getViewedAt(),
				viewedBy == null ? null : viewedBy.getId(),
				viewedBy == null ? null : viewedBy.getName(),
				application.getCreatedAt(),
				application.getUpdatedAt());
	}

	public SavedJobResponse toSavedJobResponse(SavedJob savedJob, boolean applied, long applicationCount) {
		Job job = savedJob.getJob();
		Company company = job.getCompany();
		var catalog = jobMapper.toPublicResponse(job, applied, applicationCount);
		return new SavedJobResponse(
				savedJob.getId(),
				job.getId(),
				job.getTitle(),
				company.getId(),
				company.getName(),
				company.getLogo(),
				catalog.provinces(),
				catalog.employmentTypes(),
				catalog.experienceLevels(),
				job.getExperienceYears(),
				job.getSalaryMin(),
				job.getSalaryMax(),
				job.isSalaryNegotiable(),
				job.getDeadline(),
				job.isHot(),
				job.isActive(),
				job.getStatus(),
				applied,
				applicationCount,
				savedJob.getCreatedAt());
	}
}
