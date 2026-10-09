package com.recruitment.mail;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.recruitment.entity.BusinessConfig;
import com.recruitment.enums.EmailType;
import com.recruitment.exception.BusinessRuleException;
import com.recruitment.queue.EmailPublisher.EmailMessage;
import com.recruitment.service.MessageService;

@Component
public class EmailTemplateRenderer {

	private final MessageService messageService;
	private final String layout;
	private final Map<EmailType, String> fragments;

	public EmailTemplateRenderer(MessageService messageService) {
		this.messageService = messageService;
		this.layout = readTemplate("mail/layout.html");
		Map<EmailType, String> loaded = new HashMap<>();
		for (EmailType type : EmailType.values()) {
			loaded.put(type, readTemplate("mail/" + type.templateName() + ".html"));
		}
		this.fragments = Map.copyOf(loaded);
	}

	public RenderedEmail render(EmailMessage message, BusinessConfig config, boolean embedLogo) {
		EmailType type = EmailType.fromValue(message.type())
				.orElseThrow(() -> new BusinessRuleException("error.mail.type.unknown", message.type()));
		Locale locale = resolveLocale(message.locale());
		String siteName = StringUtils.hasText(config.getSiteName()) ? config.getSiteName().trim() : "";
		Map<String, String> variables = message.variables() == null ? Map.of() : message.variables();

		String subject = messageService.get(locale, "mail." + type.templateName() + ".subject", siteName);
		String text = plainText(type, locale, siteName, variables);
		String content = fill(fragments.get(type), contentValues(type, locale, siteName, variables));
		String html = fill(layout, Map.of(
				"lang", escape(locale.getLanguage()),
				"subject", escape(subject),
				"preheader", escape(text),
				"brand", brand(config, siteName, embedLogo),
				"content", content,
				"footer", footer(config, locale, siteName)));
		return new RenderedEmail(subject, html, text);
	}

	private Map<String, String> contentValues(
			EmailType type,
			Locale locale,
			String siteName,
			Map<String, String> variables) {
		return switch (type) {
		case OTP_REGISTER -> {
			String code = required(variables, "code");
			String minutes = required(variables, "minutes");
			String prefix = "mail." + type.templateName();
			yield Map.of(
					"heading", escape(messageService.get(locale, prefix + ".heading")),
					"intro", escape(messageService.get(locale, prefix + ".intro", siteName)),
					"codeLabel", escape(messageService.get(locale, prefix + ".code-label")),
					"code", escape(code),
					"expiry", escape(messageService.get(locale, prefix + ".expiry", minutes)));
		}
		case APPLICATION_SUBMITTED -> {
			String prefix = "mail." + type.templateName();
			yield Map.of(
					"heading", escape(messageService.get(locale, prefix + ".heading")),
					"intro", escape(messageService.get(
							locale,
							prefix + ".intro",
							required(variables, "applicantName"),
							siteName)),
					"jobLabel", escape(messageService.get(locale, prefix + ".job-label")),
					"jobTitle", escape(required(variables, "jobTitle")),
					"companyLabel", escape(messageService.get(locale, prefix + ".company-label")),
					"companyName", escape(required(variables, "companyName")),
					"outro", escape(messageService.get(locale, prefix + ".outro")));
		}
		case APPLICATION_RECEIVED -> {
			String prefix = "mail." + type.templateName();
			yield Map.of(
					"heading", escape(messageService.get(locale, prefix + ".heading")),
					"intro", escape(messageService.get(
							locale,
							prefix + ".intro",
							required(variables, "applicantName"),
							siteName)),
					"applicantLabel", escape(messageService.get(locale, prefix + ".applicant-label")),
					"applicantName", escape(required(variables, "applicantName")),
					"jobLabel", escape(messageService.get(locale, prefix + ".job-label")),
					"jobTitle", escape(required(variables, "jobTitle")),
					"companyLabel", escape(messageService.get(locale, prefix + ".company-label")),
					"companyName", escape(required(variables, "companyName")),
					"outro", escape(messageService.get(locale, prefix + ".outro")));
		}
		case USER_REGISTERED -> {
			String prefix = "mail." + type.templateName();
			yield Map.of(
					"heading", escape(messageService.get(locale, prefix + ".heading")),
					"intro", escape(messageService.get(locale, prefix + ".intro", siteName)),
					"nameLabel", escape(messageService.get(locale, prefix + ".name-label")),
					"accountName", escape(required(variables, "accountName")),
					"emailLabel", escape(messageService.get(locale, prefix + ".email-label")),
					"accountEmail", escape(required(variables, "accountEmail")),
					"typeLabel", escape(messageService.get(locale, prefix + ".type-label")),
					"accountType", escape(required(variables, "accountType")),
					"outro", escape(messageService.get(locale, prefix + ".outro")));
		}
		};
	}

	private String plainText(EmailType type, Locale locale, String siteName, Map<String, String> variables) {
		return switch (type) {
		case OTP_REGISTER -> messageService.get(
				locale,
				"mail." + type.templateName() + ".text",
				siteName,
				required(variables, "code"),
				required(variables, "minutes"));
		case APPLICATION_SUBMITTED -> messageService.get(
				locale,
				"mail." + type.templateName() + ".text",
				required(variables, "applicantName"),
				required(variables, "jobTitle"),
				required(variables, "companyName"),
				siteName);
		case APPLICATION_RECEIVED -> messageService.get(
				locale,
				"mail." + type.templateName() + ".text",
				required(variables, "applicantName"),
				required(variables, "jobTitle"),
				required(variables, "companyName"),
				siteName);
		case USER_REGISTERED -> messageService.get(
				locale,
				"mail." + type.templateName() + ".text",
				required(variables, "accountType"),
				required(variables, "accountName"),
				required(variables, "accountEmail"),
				siteName);
		};
	}

	private String brand(BusinessConfig config, String siteName, boolean embedLogo) {
		StringBuilder html = new StringBuilder();
		if (embedLogo) {
			html.append(
					"<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"margin:0 0 10px;\">")
					.append("<tr><td align=\"center\">")
					.append("<img src=\"cid:logo\" alt=\"")
					.append(escape(siteName))
					.append("\" width=\"160\" style=\"width:160px;max-width:160px;height:auto;border:0;display:block;\">")
					.append("</td></tr></table>");
		}
		html.append("<div style=\"font-size:20px;line-height:1.3;font-weight:700;color:#0b1f3a;\">")
				.append(escape(siteName))
				.append("</div>");
		if (StringUtils.hasText(config.getTagline())) {
			html.append("<div style=\"margin-top:4px;font-size:13px;line-height:1.4;color:#6b7785;\">")
					.append(escape(config.getTagline().trim()))
					.append("</div>");
		}
		return html.toString();
	}

	private String footer(BusinessConfig config, Locale locale, String siteName) {
		StringBuilder html = new StringBuilder();
		if (StringUtils.hasText(config.getFooterCopyright())) {
			html.append(escape(config.getFooterCopyright().trim())).append("<br>");
		}
		html.append(escape(messageService.get(locale, "mail.footer", siteName)));
		return html.toString();
	}

	private static String required(Map<String, String> variables, String key) {
		String value = variables.get(key);
		if (!StringUtils.hasText(value)) {
			throw new BusinessRuleException("error.mail.send-failed");
		}
		return value;
	}

	private static Locale resolveLocale(String locale) {
		if (!StringUtils.hasText(locale)) {
			return Locale.forLanguageTag("vi");
		}
		return Locale.forLanguageTag(locale.trim());
	}

	private static String fill(String template, Map<String, String> values) {
		String result = template;
		for (Map.Entry<String, String> entry : values.entrySet()) {
			result = result.replace("{{" + entry.getKey() + "}}", entry.getValue());
		}
		return result;
	}

	private static String escape(String value) {
		if (value == null) {
			return "";
		}
		return value
				.replace("&", "&amp;")
				.replace("<", "&lt;")
				.replace(">", "&gt;")
				.replace("\"", "&quot;");
	}

	private static String readTemplate(String path) {
		try {
			return new ClassPathResource(path).getContentAsString(StandardCharsets.UTF_8);
		} catch (IOException ex) {
			throw new IllegalStateException("Missing mail template " + path, ex);
		}
	}
}

record RenderedEmail(String subject, String html, String text) {
}
