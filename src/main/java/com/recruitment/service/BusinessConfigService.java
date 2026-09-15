package com.recruitment.service;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.recruitment.dto.request.admin.businessconfig.UpdateBusinessConfigRequest;
import com.recruitment.dto.response.admin.businessconfig.BusinessConfigResponse;
import com.recruitment.dto.response.common.ApiResponse;
import com.recruitment.dto.response.common.BusinessConfigPublicResponse;
import com.recruitment.dto.response.common.UploadedFileResponse;
import com.recruitment.entity.BusinessConfig;
import com.recruitment.enums.UploadFolderType;
import com.recruitment.exception.BusinessRuleException;
import com.recruitment.exception.ResourceNotFoundException;
import com.recruitment.mapper.BusinessConfigMapper;
import com.recruitment.repository.BusinessConfigRepository;
import com.recruitment.util.constant.Constants;
import com.recruitment.util.helper.FileHelper;
import com.recruitment.util.helper.NumberHelper;

@Service
public class BusinessConfigService {

	private final BusinessConfigRepository businessConfigRepository;
	private final BusinessConfigMapper businessConfigMapper;
	private final MessageService messageService;
	private final FileStorageService fileStorageService;

	public BusinessConfigService(
			BusinessConfigRepository businessConfigRepository,
			BusinessConfigMapper businessConfigMapper,
			MessageService messageService,
			FileStorageService fileStorageService) {
		this.businessConfigRepository = businessConfigRepository;
		this.businessConfigMapper = businessConfigMapper;
		this.messageService = messageService;
		this.fileStorageService = fileStorageService;
	}

	public ResponseEntity<ApiResponse<BusinessConfigPublicResponse>> getPublic() {
		BusinessConfig config = getSingleton();
		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("business-config.get.success"),
				businessConfigMapper.toPublicResponse(config)));
	}

	public ResponseEntity<ApiResponse<BusinessConfigResponse>> getAdmin() {
		BusinessConfig config = getSingleton();
		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("business-config.get.success"),
				businessConfigMapper.toResponse(config)));
	}

	@Transactional
	public ResponseEntity<ApiResponse<BusinessConfigResponse>> update(
			UpdateBusinessConfigRequest request,
			String currentUsername) {

		BusinessConfig config = getSingleton();

		if (request.siteName() != null) {
			config.setSiteName(trimToNull(request.siteName()));
		}
		if (request.tagline() != null) {
			config.setTagline(trimToNull(request.tagline()));
		}
		if (request.email() != null) {
			config.setEmail(trimToNull(request.email()));
		}
		if (request.phone() != null) {
			config.setPhone(trimToNull(request.phone()));
		}
		if (request.address() != null) {
			config.setAddress(trimToNull(request.address()));
		}
		if (request.taxCode() != null) {
			config.setTaxCode(trimToNull(request.taxCode()));
		}
		if (request.latitude() != null) {
			config.setLatitude(parseCoordinate(request.latitude(), -90.0, 90.0, "validation.latitude.range"));
		}
		if (request.longitude() != null) {
			config.setLongitude(parseCoordinate(request.longitude(), -180.0, 180.0, "validation.longitude.range"));
		}

		applyImageField(
				request.logoFile(),
				request.logo(),
				config::getLogo,
				config::setLogo,
				UploadFolderType.LOGO);
		applyImageField(
				request.homeBannerFile(),
				request.homeBanner(),
				config::getHomeBanner,
				config::setHomeBanner,
				UploadFolderType.HOME_BANNER);
		applyImageField(
				request.userLoginBackgroundFile(),
				request.userLoginBackground(),
				config::getUserLoginBackground,
				config::setUserLoginBackground,
				UploadFolderType.USER_LOGIN_BACKGROUND);
		applyImageField(
				request.adminLoginBackgroundFile(),
				request.adminLoginBackground(),
				config::getAdminLoginBackground,
				config::setAdminLoginBackground,
				UploadFolderType.ADMIN_LOGIN_BACKGROUND);
		applyImageField(
				request.userRegisterBannerFile(),
				request.userRegisterBanner(),
				config::getUserRegisterBanner,
				config::setUserRegisterBanner,
				UploadFolderType.USER_REGISTER_BANNER);

		if (request.footerAbout() != null) {
			config.setFooterAbout(trimToNull(request.footerAbout()));
		}
		if (request.footerCopyright() != null) {
			config.setFooterCopyright(trimToNull(request.footerCopyright()));
		}
		if (request.footerAddress() != null) {
			config.setFooterAddress(trimToNull(request.footerAddress()));
		}
		if (request.footerPhone() != null) {
			config.setFooterPhone(trimToNull(request.footerPhone()));
		}
		if (request.footerEmail() != null) {
			config.setFooterEmail(trimToNull(request.footerEmail()));
		}
		if (request.privacyPolicy() != null) {
			config.setPrivacyPolicy(trimToNull(request.privacyPolicy()));
		}
		if (request.termsOfService() != null) {
			config.setTermsOfService(trimToNull(request.termsOfService()));
		}
		if (request.facebookUrl() != null) {
			config.setFacebookUrl(trimToNull(request.facebookUrl()));
		}
		if (request.linkedinUrl() != null) {
			config.setLinkedinUrl(trimToNull(request.linkedinUrl()));
		}
		if (request.twitterUrl() != null) {
			config.setTwitterUrl(trimToNull(request.twitterUrl()));
		}
		if (request.youtubeUrl() != null) {
			config.setYoutubeUrl(trimToNull(request.youtubeUrl()));
		}
		if (request.metaTitle() != null) {
			config.setMetaTitle(trimToNull(request.metaTitle()));
		}
		if (request.metaDescription() != null) {
			config.setMetaDescription(trimToNull(request.metaDescription()));
		}
		if (request.metaKeywords() != null) {
			config.setMetaKeywords(trimToNull(request.metaKeywords()));
		}
		if (request.smtpHost() != null) {
			config.setSmtpHost(trimToNull(request.smtpHost()));
		}
		if (request.smtpPort() != null) {
			config.setSmtpPort(request.smtpPort());
		}
		if (request.smtpUsername() != null) {
			config.setSmtpUsername(trimToNull(request.smtpUsername()));
		}
		if (StringUtils.hasText(request.smtpPassword())) {
			config.setSmtpPassword(request.smtpPassword().trim());
		}
		if (request.smtpFromEmail() != null) {
			config.setSmtpFromEmail(trimToNull(request.smtpFromEmail()));
		}
		if (request.smtpFromName() != null) {
			config.setSmtpFromName(trimToNull(request.smtpFromName()));
		}
		if (request.smtpAuth() != null) {
			config.setSmtpAuth(request.smtpAuth());
		}
		if (request.smtpStartTls() != null) {
			config.setSmtpStartTls(request.smtpStartTls());
		}

		config.setUpdatedBy(currentUsername);
		BusinessConfig saved = businessConfigRepository.save(config);

		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("business-config.update.success"),
				businessConfigMapper.toResponse(saved)));
	}

	private void applyImageField(
			MultipartFile file,
			String link,
			Supplier<String> currentGetter,
			Consumer<String> setter,
			UploadFolderType folderType) {

		boolean hasFile = FileHelper.hasFile(file);
		boolean hasLink = StringUtils.hasText(link);

		if (hasFile && hasLink) {
			throw new BusinessRuleException("error.business-config.image.file-and-link");
		}

		if (hasFile) {
			String oldUrl = currentGetter.get();
			UploadedFileResponse uploaded = fileStorageService.uploadImage(file, folderType);
			setter.accept(uploaded.url());
			if (oldUrl != null && !oldUrl.equals(uploaded.url())) {
				fileStorageService.deleteByPublicUrl(oldUrl);
			}
			return;
		}

		if (link != null) {
			setter.accept(trimToNull(link));
		}
	}

	private Double parseCoordinate(String raw, double min, double max, String rangeMessageKey) {
		try {
			Double value = NumberHelper.parseFlexibleDouble(raw);
			if (value != null && (value < min || value > max)) {
				throw new BusinessRuleException(rangeMessageKey);
			}
			return value;
		}
		catch (NumberFormatException ex) {
			throw new BusinessRuleException(
					"error.business-config.coordinate.invalid",
					raw == null ? "" : raw.trim());
		}
	}

	private BusinessConfig getSingleton() {
		return businessConfigRepository.findById(Constants.BUSINESS_CONFIG_ID)
				.orElseThrow(() -> new ResourceNotFoundException(
						"error.resource.business-config",
						"id",
						Constants.BUSINESS_CONFIG_ID));
	}

	private String trimToNull(String value) {
		if (!StringUtils.hasText(value)) {
			return null;
		}
		return value.trim();
	}
}
