package com.recruitment.service;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import com.recruitment.exception.LocalizedMessageException;

@Service
public class MessageService {

	private final MessageSource messageSource;

	public MessageService(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public String get(String code, Object... args) {
		Locale locale = LocaleContextHolder.getLocale();
		return messageSource.getMessage(code, args, locale);
	}

	public String resolveLocalizedMessage(LocalizedMessageException ex) {
		Object[] args = ex.getArgs();
		if ("error.not-found.resource".equals(ex.getMessageKey()) && args.length >= 3) {
			String resource = get((String) args[0]);
			return get(ex.getMessageKey(), resource, args[1], args[2]);
		}
		return get(ex.getMessageKey(), args);
	}
}
