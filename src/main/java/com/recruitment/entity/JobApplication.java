package com.recruitment.entity;

import java.time.Instant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.recruitment.enums.ApplicationStatusEnum;

@Entity
@Table(name = "job_applications", uniqueConstraints = {
		@UniqueConstraint(columnNames = { "job_id", "user_id" })
})
public class JobApplication extends BaseEntity {

	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "job_id", nullable = false)
	private Job job;

	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(name = "full_name", nullable = false, length = 255)
	private String fullName;

	@Column(nullable = false, length = 255)
	private String email;

	@Column(length = 20)
	private String phone;

	@Column(name = "cv_url", nullable = false, length = 1000)
	private String cvUrl;

	@Column(name = "cover_letter", length = 500)
	private String coverLetter;

	@Column(nullable = false, length = 20)
	private String status = ApplicationStatusEnum.SUBMITTED.name();

	@Column(name = "viewed_at")
	private Instant viewedAt;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "viewed_by_user_id")
	private User viewedByUser;

	public Job getJob() {
		return job;
	}

	public void setJob(Job job) {
		this.job = job;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getCvUrl() {
		return cvUrl;
	}

	public void setCvUrl(String cvUrl) {
		this.cvUrl = cvUrl;
	}

	public String getCoverLetter() {
		return coverLetter;
	}

	public void setCoverLetter(String coverLetter) {
		this.coverLetter = coverLetter;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Instant getViewedAt() {
		return viewedAt;
	}

	public void setViewedAt(Instant viewedAt) {
		this.viewedAt = viewedAt;
	}

	public User getViewedByUser() {
		return viewedByUser;
	}

	public void setViewedByUser(User viewedByUser) {
		this.viewedByUser = viewedByUser;
	}
}
