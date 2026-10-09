package com.recruitment.mapper;

import org.springframework.stereotype.Component;

import com.recruitment.dto.response.admin.businessconfig.BusinessConfigResponse;
import com.recruitment.dto.response.common.BusinessConfigPublicResponse;
import com.recruitment.entity.BusinessConfig;

@Component
public class BusinessConfigMapper {

	public BusinessConfigPublicResponse toPublicResponse(BusinessConfig config) {
		return new BusinessConfigPublicResponse(
				config.getId(),
				config.getSiteName(),
				config.getTagline(),
				config.getEmail(),
				config.getPhone(),
				config.getAddress(),
				config.getTaxCode(),
				config.getLatitude(),
				config.getLongitude(),
				config.getLogo(),
				config.getHomeBanner(),
				config.getUserLoginBackground(),
				config.getUserRegisterBanner(),
				config.getFooterAbout(),
				config.getFooterCopyright(),
				config.getFooterAddress(),
				config.getFooterPhone(),
				config.getFooterEmail(),
				config.getPrivacyPolicy(),
				config.getTermsOfService(),
				config.getFacebookUrl(),
				config.getLinkedinUrl(),
				config.getTwitterUrl(),
				config.getYoutubeUrl(),
				config.getMetaTitle(),
				config.getMetaDescription(),
				config.getMetaKeywords(),
				0L,
				0L,
				0L);
	}

	public BusinessConfigResponse toResponse(BusinessConfig config) {
		return new BusinessConfigResponse(
				config.getId(),
				config.getSiteName(),
				config.getTagline(),
				config.getEmail(),
				config.getNotificationEmails(),
				config.getPhone(),
				config.getAddress(),
				config.getTaxCode(),
				config.getLatitude(),
				config.getLongitude(),
				config.getLogo(),
				config.getHomeBanner(),
				config.getUserLoginBackground(),
				config.getAdminLoginBackground(),
				config.getUserRegisterBanner(),
				config.getFooterAbout(),
				config.getFooterCopyright(),
				config.getFooterAddress(),
				config.getFooterPhone(),
				config.getFooterEmail(),
				config.getPrivacyPolicy(),
				config.getTermsOfService(),
				config.getFacebookUrl(),
				config.getLinkedinUrl(),
				config.getTwitterUrl(),
				config.getYoutubeUrl(),
				config.getMetaTitle(),
				config.getMetaDescription(),
				config.getMetaKeywords(),
				config.getSmtpHost(),
				config.getSmtpPort(),
				config.getSmtpUsername(),
				config.getSmtpPassword(),
				config.getSmtpFromEmail(),
				config.getSmtpFromName(),
				config.isSmtpAuth(),
				config.isSmtpStartTls(),
				config.getCreatedAt(),
				config.getUpdatedAt());
	}
}
