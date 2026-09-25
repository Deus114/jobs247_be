package com.recruitment.exception;

public class AccountInactiveException extends LocalizedMessageException {

	public AccountInactiveException(String messageKey, Object... args) {
		super(messageKey, args);
	}
}
