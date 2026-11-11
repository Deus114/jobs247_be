package com.recruitment.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "job_view_histories", uniqueConstraints = {
		@UniqueConstraint(name = "uk_job_view_histories_user_job", columnNames = { "user_id", "job_id" })
}, indexes = {
		@Index(name = "idx_job_view_histories_user_viewed", columnList = "user_id, last_viewed_at")
})
public class JobViewHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "job_id", nullable = false)
	private Job job;

	@Column(name = "last_viewed_at", nullable = false)
	private Instant lastViewedAt;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@PrePersist
	protected void onCreate() {
		Instant now = Instant.now();
		createdAt = now;
		updatedAt = now;
		if (lastViewedAt == null) {
			lastViewedAt = now;
		}
	}

	@PreUpdate
	protected void onUpdate() {
		updatedAt = Instant.now();
	}

	public Long getId() {
		return id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Job getJob() {
		return job;
	}

	public void setJob(Job job) {
		this.job = job;
	}

	public Instant getLastViewedAt() {
		return lastViewedAt;
	}

	public void setLastViewedAt(Instant lastViewedAt) {
		this.lastViewedAt = lastViewedAt;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}
}
