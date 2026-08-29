package com.recruitment.exception;

public class DuplicateResourceException extends LocalizedMessageException {

	public DuplicateResourceException(String messageKey, Object... args) {
		super(messageKey, args);
	}
}
