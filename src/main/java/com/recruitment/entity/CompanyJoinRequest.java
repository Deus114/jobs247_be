package com.recruitment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import com.recruitment.enums.CompanyJoinRequestStatusEnum;

@Entity
@Table(name = "company_join_requests")
public class CompanyJoinRequest extends BaseEntity {

	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "company_id", nullable = false)
	private Company company;

	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(nullable = false, length = 20)
	private String status = CompanyJoinRequestStatusEnum.PENDING.name();

	@Column(length = 500)
	private String message;

	public Company getCompany() {
		return company;
	}

	public void setCompany(Company company) {
		this.company = company;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
