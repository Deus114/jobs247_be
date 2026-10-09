package com.recruitment.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.hibernate.annotations.BatchSize;

import com.recruitment.enums.CompanyStatusEnum;

@Entity
@Table(name = "companies")
public class Company extends BaseEntity {

	@Column(nullable = false, length = 255)
	private String name;

	@Column(length = 200)
	private String slug;

	@Column(nullable = false, length = 30)
	private String size;

	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "province_id", nullable = false)
	private Province province;

	@Column(nullable = false, length = 500)
	private String address;

	@Column(nullable = false, length = 255)
	private String email;

	@Column(nullable = false, length = 20)
	private String phone;

	@Column(length = 255)
	private String website;

	@Column(length = 1000)
	private String logo;

	@Column(name = "background_image", length = 1000)
	private String backgroundImage;

	@Column(name = "tax_code", nullable = false, unique = true, length = 20)
	private String taxCode;

	@Column(nullable = false, length = 500)
	private String description;

	@Column(nullable = false, length = 20)
	private String status = CompanyStatusEnum.PENDING.name();

	@Column(name = "rejection_reason", length = 1000)
	private String rejectionReason;

	@Column(name = "rejected_at")
	private Instant rejectedAt;

	@Column(name = "approved_at")
	private Instant approvedAt;

	@ManyToMany(fetch = FetchType.LAZY)
	@BatchSize(size = 20)
	@JoinTable(
			name = "company_industries",
			joinColumns = @JoinColumn(name = "company_id"),
			inverseJoinColumns = @JoinColumn(name = "industry_id"))
	private List<Industry> industries = new ArrayList<>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSlug() {
		return slug;
	}

	public void setSlug(String slug) {
		this.slug = slug;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public Province getProvince() {
		return province;
	}

	public void setProvince(Province province) {
		this.province = province;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
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

	public String getWebsite() {
		return website;
	}

	public void setWebsite(String website) {
		this.website = website;
	}

	public String getLogo() {
		return logo;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}

	public String getBackgroundImage() {
		return backgroundImage;
	}

	public void setBackgroundImage(String backgroundImage) {
		this.backgroundImage = backgroundImage;
	}

	public String getTaxCode() {
		return taxCode;
	}

	public void setTaxCode(String taxCode) {
		this.taxCode = taxCode;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
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

	public List<Industry> getIndustries() {
		return industries;
	}

	public void setIndustries(List<Industry> industries) {
		this.industries = industries;
	}
}
