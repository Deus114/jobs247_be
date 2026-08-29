package com.recruitment.exception;

public class FileValidationException extends LocalizedMessageException {

	public FileValidationException(String messageKey, Object... args) {
		super(messageKey, args);
	}
}
