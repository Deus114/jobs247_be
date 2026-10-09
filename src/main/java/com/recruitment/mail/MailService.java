package com.recruitment.mail;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.recruitment.entity.BusinessConfig;
import com.recruitment.exception.BusinessRuleException;
import com.recruitment.queue.EmailPublisher.EmailMessage;
import com.recruitment.repository.BusinessConfigRepository;
import com.recruitment.service.S3StorageService;
import com.recruitment.service.S3StorageService.StoredObject;
import com.recruitment.util.constant.Constants;

@Service
public class MailService {

	private static final Logger log = LoggerFactory.getLogger(MailService.class);
	private static final String LOGO_CONTENT_ID = "logo";

	private final BusinessConfigRepository businessConfigRepository;
	private final EmailTemplateRenderer emailTemplateRenderer;
	private final S3StorageService s3StorageService;

	public MailService(
			BusinessConfigRepository businessConfigRepository,
			EmailTemplateRenderer emailTemplateRenderer,
			S3StorageService s3StorageService) {
		this.businessConfigRepository = businessConfigRepository;
		this.emailTemplateRenderer = emailTemplateRenderer;
		this.s3StorageService = s3StorageService;
	}

	public void send(EmailMessage message) {
		BusinessConfig config = businessConfigRepository.findById(Constants.BUSINESS_CONFIG_ID)
				.orElseThrow(() -> new BusinessRuleException("error.resource.business-config"));

		if (!StringUtils.hasText(config.getSmtpHost()) || config.getSmtpPort() == null) {
			throw new BusinessRuleException("error.mail.smtp-not-configured");
		}

		Optional<StoredObject> logo = loadLogo(config.getLogo());
		RenderedEmail rendered = emailTemplateRenderer.render(message, config, logo.isPresent());

		JavaMailSenderImpl sender = new JavaMailSenderImpl();
		sender.setHost(config.getSmtpHost());
		sender.setPort(config.getSmtpPort());
		sender.setDefaultEncoding(StandardCharsets.UTF_8.name());
		if (StringUtils.hasText(config.getSmtpUsername())) {
			sender.setUsername(config.getSmtpUsername());
		}
		if (StringUtils.hasText(config.getSmtpPassword())) {
			sender.setPassword(config.getSmtpPassword());
		}

		java.util.Properties props = sender.getJavaMailProperties();
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.auth", String.valueOf(config.isSmtpAuth()));
		props.put("mail.smtp.starttls.enable", String.valueOf(config.isSmtpStartTls()));

		try {
			MimeMessage mimeMessage = sender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());
			String from = StringUtils.hasText(config.getSmtpFromEmail())
					? config.getSmtpFromEmail()
					: config.getSmtpUsername();
			if (StringUtils.hasText(config.getSmtpFromName()) && StringUtils.hasText(from)) {
				helper.setFrom(from, config.getSmtpFromName());
			} else if (StringUtils.hasText(from)) {
				helper.setFrom(from);
			}
			helper.setTo(message.to());
			helper.setSubject(rendered.subject());
			helper.setText(rendered.text(), rendered.html());
			if (logo.isPresent()) {
				StoredObject image = logo.get();
				helper.addInline(
						LOGO_CONTENT_ID,
						new ByteArrayResource(image.content()),
						contentType(image.contentType()));
			}
			sender.send(mimeMessage);
			log.info("Email {} sent to {}", message.type(), message.to());
		} catch (MailException | MessagingException | UnsupportedEncodingException ex) {
			log.error("Failed to send email {} to {}: {}", message.type(), message.to(), ex.getMessage());
			throw new BusinessRuleException("error.mail.send-failed");
		}
	}

	private Optional<StoredObject> loadLogo(String logo) {
		if (!StringUtils.hasText(logo)) {
			return Optional.empty();
		}
		String url = logo.trim();
		if (!url.startsWith("https://") && !url.startsWith("http://")) {
			return Optional.empty();
		}
		Optional<String> objectKey = s3StorageService.toObjectKey(url);
		if (objectKey.isPresent()) {
			return s3StorageService.findObject(objectKey.get());
		}
		return downloadLogo(url);
	}

	private Optional<StoredObject> downloadLogo(String url) {
		try {
			HttpRequest request = HttpRequest.newBuilder(URI.create(url))
					.timeout(Duration.ofSeconds(10))
					.GET()
					.build();
			HttpResponse<byte[]> response = HttpClient.newBuilder()
					.connectTimeout(Duration.ofSeconds(5))
					.followRedirects(HttpClient.Redirect.NORMAL)
					.build()
					.send(request, HttpResponse.BodyHandlers.ofByteArray());
			if (response.statusCode() < 200 || response.statusCode() >= 300 || response.body().length == 0) {
				log.warn("Logo URL returned status {} for {}", response.statusCode(), url);
				return Optional.empty();
			}
			String type = response.headers().firstValue("Content-Type").orElse("image/png");
			return Optional.of(new StoredObject(response.body(), type));
		} catch (IOException | InterruptedException ex) {
			if (ex instanceof InterruptedException) {
				Thread.currentThread().interrupt();
			}
			log.warn("Failed to download logo from {}", url);
			return Optional.empty();
		}
	}

	private static String contentType(String contentType) {
		if (!StringUtils.hasText(contentType)) {
			return "image/png";
		}
		String type = contentType.split(";", 2)[0].trim();
		if (!type.startsWith("image/")) {
			return "image/png";
		}
		return type;
	}
}
