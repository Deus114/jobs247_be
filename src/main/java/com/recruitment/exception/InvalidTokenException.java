package com.recruitment.exception;

public class InvalidTokenException extends LocalizedMessageException {

	public InvalidTokenException(String messageKey) {
		super(messageKey);
	}
}
