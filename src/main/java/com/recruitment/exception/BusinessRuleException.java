package com.recruitment.exception;

public class BusinessRuleException extends LocalizedMessageException {

	public BusinessRuleException(String messageKey, Object... args) {
		super(messageKey, args);
	}
}
