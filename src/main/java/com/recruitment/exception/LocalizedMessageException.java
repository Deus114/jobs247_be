package com.recruitment.exception;

public abstract class LocalizedMessageException extends RuntimeException {

	private final String messageKey;
	private final Object[] args;

	protected LocalizedMessageException(String messageKey, Object... args) {
		super(messageKey);
		this.messageKey = messageKey;
		this.args = args;
	}

	public String getMessageKey() {
		return messageKey;
	}

	public Object[] getArgs() {
		return args;
	}
}
