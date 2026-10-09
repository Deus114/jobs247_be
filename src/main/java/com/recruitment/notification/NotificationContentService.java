package com.recruitment.notification;

import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.recruitment.entity.Notification;
import com.recruitment.service.MessageService;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@Service
public class NotificationContentService {

	private static final Logger log = LoggerFactory.getLogger(NotificationContentService.class);
	private static final Locale PUSH_LOCALE = Locale.forLanguageTag("vi");

	private final MessageService messageService;
	private final JsonMapper jsonMapper;

	public NotificationContentService(MessageService messageService, JsonMapper jsonMapper) {
		this.messageService = messageService;
		this.jsonMapper = jsonMapper;
	}

	public String encodeArgs(List<String> args) {
		if (args == null || args.isEmpty()) {
			return null;
		}
		return jsonMapper.writeValueAsString(args);
	}

	public String title(Notification notification) {
		return title(notification, LocaleContextHolder.getLocale());
	}

	public String body(Notification notification) {
		return body(notification, LocaleContextHolder.getLocale());
	}

	public String titleForPush(Notification notification) {
		return title(notification, PUSH_LOCALE);
	}

	public String bodyForPush(Notification notification) {
		return body(notification, PUSH_LOCALE);
	}

	private String title(Notification notification, Locale locale) {
		return messageService.get(locale, notification.getTitleKey());
	}

	private String body(Notification notification, Locale locale) {
		return messageService.get(locale, notification.getBodyKey(), readArgs(notification.getBodyArgs()));
	}

	private Object[] readArgs(String raw) {
		if (!StringUtils.hasText(raw)) {
			return new Object[0];
		}
		try {
			String[] args = jsonMapper.readValue(raw, String[].class);
			return args == null ? new Object[0] : args;
		} catch (JacksonException ex) {
			log.warn("Cannot read notification body args");
			return new Object[0];
		}
	}
}
