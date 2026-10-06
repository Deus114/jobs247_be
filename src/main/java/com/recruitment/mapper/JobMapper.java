package com.recruitment.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.recruitment.dto.response.admin.job.AdminJobResponse;
import com.recruitment.dto.response.common.JobCatalogItemResponse;
import com.recruitment.dto.response.common.PublicJobResponse;
import com.recruitment.dto.response.user.job.EmployerJobResponse;
import com.recruitment.entity.Company;
import com.recruitment.entity.EducationLevel;
import com.recruitment.entity.Industry;
import com.recruitment.entity.Job;
import com.recruitment.entity.Province;
import com.recruitment.entity.User;
import com.recruitment.util.helper.LocaleTextHelper;

@Component
public class JobMapper {

	public EmployerJobResponse toEmployerResponse(Job job, long applicationCount) {
		Company company = job.getCompany();
		User creator = job.getCreatedByUser();
		return new EmployerJobResponse(
				job.getId(),
				company.getId(),
				company.getName(),
				company.getLogo(),
				creator.getId(),
				creator.getName(),
				job.getTitle(),
				mapIndustries(job.getIndustries()),
				mapEducationLevels(job.getEducationLevels()),
				mapProvinces(job.getProvinces()),
				List.copyOf(job.getEmploymentTypes()),
				List.copyOf(job.getExperienceLevels()),
				job.getExperienceYears(),
				job.getSalaryMin(),
				job.getSalaryMax(),
				job.isSalaryNegotiable(),
				job.getDeadline(),
				job.getWorkLocation(),
				job.getWorkingTime(),
				job.getDescription(),
				job.getRequirements(),
				job.getBenefits(),
				job.getApplicantQuestion(),
				job.isHot(),
				applicationCount,
				job.getStatus(),
				job.getRejectionReason(),
				job.getRejectedAt(),
				job.getApprovedAt(),
				job.isActive(),
				job.getCreatedAt(),
				job.getUpdatedAt());
	}

	public AdminJobResponse toAdminResponse(Job job, long applicationCount) {
		Company company = job.getCompany();
		User creator = job.getCreatedByUser();
		return new AdminJobResponse(
				job.getId(),
				company.getId(),
				company.getName(),
				company.getLogo(),
				creator.getId(),
				creator.getName(),
				job.getTitle(),
				mapIndustries(job.getIndustries()),
				mapEducationLevels(job.getEducationLevels()),
				mapProvinces(job.getProvinces()),
				List.copyOf(job.getEmploymentTypes()),
				List.copyOf(job.getExperienceLevels()),
				job.getExperienceYears(),
				job.getSalaryMin(),
				job.getSalaryMax(),
				job.isSalaryNegotiable(),
				job.getDeadline(),
				job.getWorkLocation(),
				job.getWorkingTime(),
				job.getDescription(),
				job.getRequirements(),
				job.getBenefits(),
				job.getApplicantQuestion(),
				job.isHot(),
				applicationCount,
				job.getStatus(),
				job.getRejectionReason(),
				job.getRejectedAt(),
				job.getApprovedAt(),
				job.isActive(),
				job.getCreatedAt(),
				job.getUpdatedAt());
	}

	public PublicJobResponse toPublicResponse(Job job, boolean applied, long applicationCount) {
		Company company = job.getCompany();
		return new PublicJobResponse(
				job.getId(),
				company.getId(),
				company.getName(),
				company.getLogo(),
				company.getAddress(),
				job.getTitle(),
				mapIndustries(job.getIndustries()),
				mapEducationLevels(job.getEducationLevels()),
				mapProvinces(job.getProvinces()),
				List.copyOf(job.getEmploymentTypes()),
				List.copyOf(job.getExperienceLevels()),
				job.getExperienceYears(),
				job.getSalaryMin(),
				job.getSalaryMax(),
				job.isSalaryNegotiable(),
				job.getDeadline(),
				job.getWorkLocation(),
				job.getWorkingTime(),
				job.getDescription(),
				job.getRequirements(),
				job.getBenefits(),
				job.getApplicantQuestion(),
				job.isHot(),
				applied,
				applicationCount,
				job.getCreatedAt(),
				job.getUpdatedAt());
	}

	private List<JobCatalogItemResponse> mapIndustries(List<Industry> industries) {
		return industries.stream()
				.map(industry -> new JobCatalogItemResponse(
						industry.getId(),
						LocaleTextHelper.pick(industry.getNameVi(), industry.getNameEn())))
				.toList();
	}

	private List<JobCatalogItemResponse> mapEducationLevels(List<EducationLevel> educationLevels) {
		return educationLevels.stream()
				.map(level -> new JobCatalogItemResponse(
						level.getId(),
						LocaleTextHelper.pick(level.getNameVi(), level.getNameEn())))
				.toList();
	}

	private List<JobCatalogItemResponse> mapProvinces(List<Province> provinces) {
		return provinces.stream()
				.map(province -> new JobCatalogItemResponse(province.getId(), province.getName()))
				.toList();
	}
}
