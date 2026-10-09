package com.recruitment.entity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import org.hibernate.annotations.BatchSize;

import com.recruitment.enums.JobStatusEnum;

@Entity
@Table(name = "jobs")
public class Job extends BaseEntity {

	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "company_id", nullable = false)
	private Company company;

	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "created_by_user_id", nullable = false)
	private User createdByUser;

	@Column(nullable = false, length = 255)
	private String title;

	@Column(length = 200)
	private String slug;

	@ManyToMany(fetch = FetchType.LAZY)
	@BatchSize(size = 20)
	@JoinTable(
			name = "job_industries",
			joinColumns = @JoinColumn(name = "job_id"),
			inverseJoinColumns = @JoinColumn(name = "industry_id"))
	private List<Industry> industries = new ArrayList<>();

	@ManyToMany(fetch = FetchType.LAZY)
	@BatchSize(size = 20)
	@JoinTable(
			name = "job_education_levels",
			joinColumns = @JoinColumn(name = "job_id"),
			inverseJoinColumns = @JoinColumn(name = "education_level_id"))
	private List<EducationLevel> educationLevels = new ArrayList<>();

	@ManyToMany(fetch = FetchType.LAZY)
	@BatchSize(size = 20)
	@JoinTable(
			name = "job_provinces",
			joinColumns = @JoinColumn(name = "job_id"),
			inverseJoinColumns = @JoinColumn(name = "province_id"))
	private List<Province> provinces = new ArrayList<>();

	@ElementCollection(fetch = FetchType.LAZY)
	@BatchSize(size = 20)
	@CollectionTable(name = "job_employment_types", joinColumns = @JoinColumn(name = "job_id"))
	@OrderColumn(name = "sort_order")
	@Column(name = "employment_type", nullable = false, length = 30)
	private List<String> employmentTypes = new ArrayList<>();

	@ElementCollection(fetch = FetchType.LAZY)
	@BatchSize(size = 20)
	@CollectionTable(name = "job_experience_levels", joinColumns = @JoinColumn(name = "job_id"))
	@OrderColumn(name = "sort_order")
	@Column(name = "experience_level", nullable = false, length = 30)
	private List<String> experienceLevels = new ArrayList<>();

	@Column(name = "experience_years")
	private Integer experienceYears;

	@Column(name = "salary_min")
	private Integer salaryMin;

	@Column(name = "salary_max")
	private Integer salaryMax;

	@Column(name = "salary_negotiable", nullable = false)
	private boolean salaryNegotiable = false;

	@Column(nullable = false)
	private LocalDate deadline;

	@Column(name = "work_location", columnDefinition = "TEXT")
	private String workLocation;

	@Column(name = "working_time", columnDefinition = "TEXT")
	private String workingTime;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String description;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String requirements;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String benefits;

	@Column(name = "applicant_question", length = 1000)
	private String applicantQuestion;

	@Column(name = "is_hot", nullable = false)
	private boolean hot = false;

	@Column(nullable = false, length = 20)
	private String status = JobStatusEnum.PENDING.name();

	@Column(name = "rejection_reason", length = 1000)
	private String rejectionReason;

	@Column(name = "rejected_at")
	private Instant rejectedAt;

	@Column(name = "approved_at")
	private Instant approvedAt;

	public Company getCompany() {
		return company;
	}

	public void setCompany(Company company) {
		this.company = company;
	}

	public User getCreatedByUser() {
		return createdByUser;
	}

	public void setCreatedByUser(User createdByUser) {
		this.createdByUser = createdByUser;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSlug() {
		return slug;
	}

	public void setSlug(String slug) {
		this.slug = slug;
	}

	public List<Industry> getIndustries() {
		return industries;
	}

	public void setIndustries(List<Industry> industries) {
		this.industries.clear();
		if (industries != null) {
			this.industries.addAll(industries);
		}
	}

	public List<EducationLevel> getEducationLevels() {
		return educationLevels;
	}

	public void setEducationLevels(List<EducationLevel> educationLevels) {
		this.educationLevels.clear();
		if (educationLevels != null) {
			this.educationLevels.addAll(educationLevels);
		}
	}

	public List<Province> getProvinces() {
		return provinces;
	}

	public void setProvinces(List<Province> provinces) {
		this.provinces.clear();
		if (provinces != null) {
			this.provinces.addAll(provinces);
		}
	}

	public List<String> getEmploymentTypes() {
		return employmentTypes;
	}

	public void setEmploymentTypes(List<String> employmentTypes) {
		this.employmentTypes.clear();
		if (employmentTypes != null) {
			this.employmentTypes.addAll(employmentTypes);
		}
	}

	public List<String> getExperienceLevels() {
		return experienceLevels;
	}

	public void setExperienceLevels(List<String> experienceLevels) {
		this.experienceLevels.clear();
		if (experienceLevels != null) {
			this.experienceLevels.addAll(experienceLevels);
		}
	}

	public Integer getExperienceYears() {
		return experienceYears;
	}

	public void setExperienceYears(Integer experienceYears) {
		this.experienceYears = experienceYears;
	}

	public Integer getSalaryMin() {
		return salaryMin;
	}

	public void setSalaryMin(Integer salaryMin) {
		this.salaryMin = salaryMin;
	}

	public Integer getSalaryMax() {
		return salaryMax;
	}

	public void setSalaryMax(Integer salaryMax) {
		this.salaryMax = salaryMax;
	}

	public boolean isSalaryNegotiable() {
		return salaryNegotiable;
	}

	public void setSalaryNegotiable(boolean salaryNegotiable) {
		this.salaryNegotiable = salaryNegotiable;
	}

	public LocalDate getDeadline() {
		return deadline;
	}

	public void setDeadline(LocalDate deadline) {
		this.deadline = deadline;
	}

	public String getWorkLocation() {
		return workLocation;
	}

	public void setWorkLocation(String workLocation) {
		this.workLocation = workLocation;
	}

	public String getWorkingTime() {
		return workingTime;
	}

	public void setWorkingTime(String workingTime) {
		this.workingTime = workingTime;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getRequirements() {
		return requirements;
	}

	public void setRequirements(String requirements) {
		this.requirements = requirements;
	}

	public String getBenefits() {
		return benefits;
	}

	public void setBenefits(String benefits) {
		this.benefits = benefits;
	}

	public String getApplicantQuestion() {
		return applicantQuestion;
	}

	public void setApplicantQuestion(String applicantQuestion) {
		this.applicantQuestion = applicantQuestion;
	}

	public boolean isHot() {
		return hot;
	}

	public void setHot(boolean hot) {
		this.hot = hot;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getRejectionReason() {
		return rejectionReason;
	}

	public void setRejectionReason(String rejectionReason) {
		this.rejectionReason = rejectionReason;
	}

	public Instant getRejectedAt() {
		return rejectedAt;
	}

	public void setRejectedAt(Instant rejectedAt) {
		this.rejectedAt = rejectedAt;
	}

	public Instant getApprovedAt() {
		return approvedAt;
	}

	public void setApprovedAt(Instant approvedAt) {
		this.approvedAt = approvedAt;
	}
}
