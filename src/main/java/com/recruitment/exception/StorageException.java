package com.recruitment.exception;

public class StorageException extends LocalizedMessageException {

	public StorageException(String messageKey, Object... args) {
		super(messageKey, args);
	}

	public StorageException(String messageKey, Throwable cause, Object... args) {
		super(messageKey, args);
		initCause(cause);
	}
}
