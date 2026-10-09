package com.recruitment.entity;

import java.time.Instant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "business_configs")
public class BusinessConfig {

	@Id
	private Long id;

	// --- Thông tin chung ---

	@Column(length = 255)
	private String siteName;

	@Column(length = 500)
	private String tagline;

	@Column(length = 255)
	private String email;

	@Column(name = "notification_emails", length = 2000)
	private String notificationEmails;

	@Column(length = 50)
	private String phone;

	@Column(length = 500)
	private String address;

	@Column(length = 50)
	private String taxCode;

	private Double latitude;

	private Double longitude;

	// --- Logo & ảnh nền (lưu URL sau upload hoặc link trực tiếp) ---

	@Column(length = 1000)
	private String logo;

	@Column(length = 1000)
	private String homeBanner;

	@Column(length = 1000)
	private String userLoginBackground;

	@Column(length = 1000)
	private String adminLoginBackground;

	@Column(length = 1000)
	private String userRegisterBanner;

	// --- Footer ---

	@Lob
	@Column(columnDefinition = "TEXT")
	private String footerAbout;

	@Column(length = 500)
	private String footerCopyright;

	@Column(length = 500)
	private String footerAddress;

	@Column(length = 50)
	private String footerPhone;

	@Column(length = 255)
	private String footerEmail;

	@Lob
	@Column(columnDefinition = "LONGTEXT")
	private String privacyPolicy;

	@Lob
	@Column(columnDefinition = "LONGTEXT")
	private String termsOfService;

	// --- Mạng xã hội ---

	@Column(length = 500)
	private String facebookUrl;

	@Column(length = 500)
	private String linkedinUrl;

	@Column(length = 500)
	private String twitterUrl;

	@Column(length = 500)
	private String youtubeUrl;

	// --- SEO & Meta ---

	@Column(length = 60)
	private String metaTitle;

	@Column(length = 160)
	private String metaDescription;

	@Column(length = 500)
	private String metaKeywords;

	// --- SMTP ---

	@Column(length = 255)
	private String smtpHost;

	private Integer smtpPort;

	@Column(length = 255)
	private String smtpUsername;

	@Column(length = 255)
	private String smtpPassword;

	@Column(length = 255)
	private String smtpFromEmail;

	@Column(length = 255)
	private String smtpFromName;

	@Column(nullable = false)
	private boolean smtpAuth = true;

	@Column(nullable = false)
	private boolean smtpStartTls = true;

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	@Column(nullable = false)
	private Instant updatedAt;

	@Column(length = 100)
	private String updatedBy;

	@PrePersist
	protected void onCreate() {
		Instant now = Instant.now();
		createdAt = now;
		updatedAt = now;
	}

	@PreUpdate
	protected void onUpdate() {
		updatedAt = Instant.now();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getSiteName() {
		return siteName;
	}

	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}

	public String getTagline() {
		return tagline;
	}

	public void setTagline(String tagline) {
		this.tagline = tagline;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getNotificationEmails() {
		return notificationEmails;
	}

	public void setNotificationEmails(String notificationEmails) {
		this.notificationEmails = notificationEmails;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getTaxCode() {
		return taxCode;
	}

	public void setTaxCode(String taxCode) {
		this.taxCode = taxCode;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public String getLogo() {
		return logo;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}

	public String getHomeBanner() {
		return homeBanner;
	}

	public void setHomeBanner(String homeBanner) {
		this.homeBanner = homeBanner;
	}

	public String getUserLoginBackground() {
		return userLoginBackground;
	}

	public void setUserLoginBackground(String userLoginBackground) {
		this.userLoginBackground = userLoginBackground;
	}

	public String getAdminLoginBackground() {
		return adminLoginBackground;
	}

	public void setAdminLoginBackground(String adminLoginBackground) {
		this.adminLoginBackground = adminLoginBackground;
	}

	public String getUserRegisterBanner() {
		return userRegisterBanner;
	}

	public void setUserRegisterBanner(String userRegisterBanner) {
		this.userRegisterBanner = userRegisterBanner;
	}

	public String getFooterAbout() {
		return footerAbout;
	}

	public void setFooterAbout(String footerAbout) {
		this.footerAbout = footerAbout;
	}

	public String getFooterCopyright() {
		return footerCopyright;
	}

	public void setFooterCopyright(String footerCopyright) {
		this.footerCopyright = footerCopyright;
	}

	public String getFooterAddress() {
		return footerAddress;
	}

	public void setFooterAddress(String footerAddress) {
		this.footerAddress = footerAddress;
	}

	public String getFooterPhone() {
		return footerPhone;
	}

	public void setFooterPhone(String footerPhone) {
		this.footerPhone = footerPhone;
	}

	public String getFooterEmail() {
		return footerEmail;
	}

	public void setFooterEmail(String footerEmail) {
		this.footerEmail = footerEmail;
	}

	public String getPrivacyPolicy() {
		return privacyPolicy;
	}

	public void setPrivacyPolicy(String privacyPolicy) {
		this.privacyPolicy = privacyPolicy;
	}

	public String getTermsOfService() {
		return termsOfService;
	}

	public void setTermsOfService(String termsOfService) {
		this.termsOfService = termsOfService;
	}

	public String getFacebookUrl() {
		return facebookUrl;
	}

	public void setFacebookUrl(String facebookUrl) {
		this.facebookUrl = facebookUrl;
	}

	public String getLinkedinUrl() {
		return linkedinUrl;
	}

	public void setLinkedinUrl(String linkedinUrl) {
		this.linkedinUrl = linkedinUrl;
	}

	public String getTwitterUrl() {
		return twitterUrl;
	}

	public void setTwitterUrl(String twitterUrl) {
		this.twitterUrl = twitterUrl;
	}

	public String getYoutubeUrl() {
		return youtubeUrl;
	}

	public void setYoutubeUrl(String youtubeUrl) {
		this.youtubeUrl = youtubeUrl;
	}

	public String getMetaTitle() {
		return metaTitle;
	}

	public void setMetaTitle(String metaTitle) {
		this.metaTitle = metaTitle;
	}

	public String getMetaDescription() {
		return metaDescription;
	}

	public void setMetaDescription(String metaDescription) {
		this.metaDescription = metaDescription;
	}

	public String getMetaKeywords() {
		return metaKeywords;
	}

	public void setMetaKeywords(String metaKeywords) {
		this.metaKeywords = metaKeywords;
	}

	public String getSmtpHost() {
		return smtpHost;
	}

	public void setSmtpHost(String smtpHost) {
		this.smtpHost = smtpHost;
	}

	public Integer getSmtpPort() {
		return smtpPort;
	}

	public void setSmtpPort(Integer smtpPort) {
		this.smtpPort = smtpPort;
	}

	public String getSmtpUsername() {
		return smtpUsername;
	}

	public void setSmtpUsername(String smtpUsername) {
		this.smtpUsername = smtpUsername;
	}

	public String getSmtpPassword() {
		return smtpPassword;
	}

	public void setSmtpPassword(String smtpPassword) {
		this.smtpPassword = smtpPassword;
	}

	public String getSmtpFromEmail() {
		return smtpFromEmail;
	}

	public void setSmtpFromEmail(String smtpFromEmail) {
		this.smtpFromEmail = smtpFromEmail;
	}

	public String getSmtpFromName() {
		return smtpFromName;
	}

	public void setSmtpFromName(String smtpFromName) {
		this.smtpFromName = smtpFromName;
	}

	public boolean isSmtpAuth() {
		return smtpAuth;
	}

	public void setSmtpAuth(boolean smtpAuth) {
		this.smtpAuth = smtpAuth;
	}

	public boolean isSmtpStartTls() {
		return smtpStartTls;
	}

	public void setSmtpStartTls(boolean smtpStartTls) {
		this.smtpStartTls = smtpStartTls;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}
}
