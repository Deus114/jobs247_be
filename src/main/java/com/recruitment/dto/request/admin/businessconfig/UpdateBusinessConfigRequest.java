package com.recruitment.dto.request.admin.businessconfig;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.media.Schema;

public record UpdateBusinessConfigRequest(
		@Size(max = 255, message = "{validation.site-name.max-length}")
		String siteName,

		@Size(max = 500, message = "{validation.tagline.max-length}")
		String tagline,

		@Email(message = "{validation.email.invalid}")
		@Size(max = 255, message = "{validation.email.max-length}")
		String email,

		@Size(max = 2000, message = "{validation.notification-emails.max-length}")
		@Schema(description = "Danh sách email nhận thông báo khi có tài khoản đăng ký mới, cách nhau bằng dấu phẩy. Ví dụ abc@gmail.com, def@gmail.com")
		String notificationEmails,

		@Size(max = 50, message = "{validation.phone.max-length}")
		String phone,

		@Size(max = 500, message = "{validation.address.max-length}")
		String address,

		@Size(max = 50, message = "{validation.tax-code.max-length}")
		String taxCode,

		@Schema(description = "Vĩ độ (chấp nhận 10.775 hoặc 10,775)")
		String latitude,

		@Schema(description = "Kinh độ (chấp nhận 106.701 hoặc 106,701)")
		String longitude,

		@Schema(type = "string", format = "binary", description = "Logo dự án (file). Không gửi kèm logo")
		MultipartFile logoFile,

		@Size(max = 1000, message = "{validation.image-url.max-length}")
		@Schema(description = "Logo dự án (link trực tiếp). Không gửi kèm logoFile")
		String logo,

		@Schema(type = "string", format = "binary", description = "Banner trang chủ (file). Không gửi kèm homeBanner")
		MultipartFile homeBannerFile,

		@Size(max = 1000, message = "{validation.image-url.max-length}")
		@Schema(description = "Banner trang chủ (link trực tiếp). Không gửi kèm homeBannerFile")
		String homeBanner,

		@Schema(type = "string", format = "binary", description = "Ảnh nền login user (file)")
		MultipartFile userLoginBackgroundFile,

		@Size(max = 1000, message = "{validation.image-url.max-length}")
		String userLoginBackground,

		@Schema(type = "string", format = "binary", description = "Ảnh nền login admin (file)")
		MultipartFile adminLoginBackgroundFile,

		@Size(max = 1000, message = "{validation.image-url.max-length}")
		String adminLoginBackground,

		@Schema(type = "string", format = "binary", description = "Banner trang đăng ký user (file)")
		MultipartFile userRegisterBannerFile,

		@Size(max = 1000, message = "{validation.image-url.max-length}")
		String userRegisterBanner,

		String footerAbout,

		@Size(max = 500, message = "{validation.footer-copyright.max-length}")
		String footerCopyright,

		@Size(max = 500, message = "{validation.address.max-length}")
		String footerAddress,

		@Size(max = 50, message = "{validation.phone.max-length}")
		String footerPhone,

		@Email(message = "{validation.email.invalid}")
		@Size(max = 255, message = "{validation.email.max-length}")
		String footerEmail,

		@Schema(description = "Nội dung HTML chính sách bảo mật (từ editor)")
		String privacyPolicy,

		@Schema(description = "Nội dung HTML điều khoản sử dụng (từ editor)")
		String termsOfService,

		@Size(max = 500, message = "{validation.social-url.max-length}")
		String facebookUrl,

		@Size(max = 500, message = "{validation.social-url.max-length}")
		String linkedinUrl,

		@Size(max = 500, message = "{validation.social-url.max-length}")
		String twitterUrl,

		@Size(max = 500, message = "{validation.social-url.max-length}")
		String youtubeUrl,

		@Size(max = 60, message = "{validation.meta-title.max-length}")
		String metaTitle,

		@Size(max = 160, message = "{validation.meta-description.max-length}")
		String metaDescription,

		@Size(max = 500, message = "{validation.meta-keywords.max-length}")
		String metaKeywords,

		@Size(max = 255, message = "{validation.smtp-host.max-length}")
		String smtpHost,

		@Min(value = 1, message = "{validation.smtp-port.range}")
		@Max(value = 65535, message = "{validation.smtp-port.range}")
		Integer smtpPort,

		@Size(max = 255, message = "{validation.smtp-username.max-length}")
		String smtpUsername,

		@Size(max = 255, message = "{validation.smtp-password.max-length}")
		String smtpPassword,

		@Email(message = "{validation.email.invalid}")
		@Size(max = 255, message = "{validation.smtp-from-email.max-length}")
		String smtpFromEmail,

		@Size(max = 255, message = "{validation.smtp-from-name.max-length}")
		String smtpFromName,

		Boolean smtpAuth,

		Boolean smtpStartTls) {
}
