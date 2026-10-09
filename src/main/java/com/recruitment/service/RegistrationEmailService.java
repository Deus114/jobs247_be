package com.recruitment.service;

import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.recruitment.entity.BusinessConfig;
import com.recruitment.entity.User;
import com.recruitment.enums.EmailType;
import com.recruitment.enums.UserTypeEnum;
import com.recruitment.queue.EmailPublisher;
import com.recruitment.repository.BusinessConfigRepository;
import com.recruitment.util.constant.Constants;

@Service
public class RegistrationEmailService {

	private static final Locale LOCALE = Locale.forLanguageTag("vi");

	private final BusinessConfigRepository businessConfigRepository;
	private final EmailPublisher emailPublisher;
	private final MessageService messageService;

	public RegistrationEmailService(
			BusinessConfigRepository businessConfigRepository,
			EmailPublisher emailPublisher,
			MessageService messageService) {
		this.businessConfigRepository = businessConfigRepository;
		this.emailPublisher = emailPublisher;
		this.messageService = messageService;
	}

	public void publishNewAccount(User user) {
		BusinessConfig config = businessConfigRepository.findById(Constants.BUSINESS_CONFIG_ID).orElse(null);
		if (config == null || !StringUtils.hasText(config.getNotificationEmails())) {
			return;
		}
		String typeKey = UserTypeEnum.EMPLOYER.name().equals(user.getType())
				? "mail.user-registered.type.employer"
				: "mail.user-registered.type.job-seeker";
		Map<String, String> variables = Map.of(
				"accountName", user.getName(),
				"accountEmail", user.getEmail(),
				"accountType", messageService.get(LOCALE, typeKey));
		for (String recipient : config.getNotificationEmails().split(",")) {
			String email = recipient.trim();
			if (!email.isEmpty()) {
				emailPublisher.publish(
						EmailType.USER_REGISTERED.name(),
						email,
						LOCALE.getLanguage(),
						variables);
			}
		}
	}
}
