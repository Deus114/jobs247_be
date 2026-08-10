package com.recruitment.exception;

public class ResourceNotFoundException extends LocalizedMessageException {

	public ResourceNotFoundException(String resourceMessageKey, String field, Object value) {
		super("error.not-found.resource", resourceMessageKey, field, value);
	}
}
