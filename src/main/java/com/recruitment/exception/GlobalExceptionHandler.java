package com.recruitment.exception;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.recruitment.dto.response.ApiResponse;
import com.recruitment.service.MessageService;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	private final MessageService messageService;

	public GlobalExceptionHandler(MessageService messageService) {
		this.messageService = messageService;
	}

	@ExceptionHandler(LocalizedMessageException.class)
	public ResponseEntity<ApiResponse<Void>> handleLocalizedMessage(LocalizedMessageException ex) {
		log.warn("{}: {}", ex.getClass().getSimpleName(), ex.getMessageKey());
		String message = messageService.resolveLocalizedMessage(ex);
		if (ex instanceof InvalidTokenException) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(ApiResponse.unauthorized(message));
		}
		if (ex instanceof ResourceNotFoundException) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(ApiResponse.notFound(message));
		}
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(ApiResponse.badRequest(message));
	}

	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<ApiResponse<Void>> handleBadCredentials(BadCredentialsException ex) {
		log.warn("BadCredentialsException: {}", ex.getMessage());
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(ApiResponse.unauthorized(messageService.get("error.unauthorized.invalid-credentials")));
	}

	@ExceptionHandler(DisabledException.class)
	public ResponseEntity<ApiResponse<Void>> handleDisabled(DisabledException ex) {
		log.warn("DisabledException: {}", ex.getMessage());
		return ResponseEntity.status(HttpStatus.FORBIDDEN)
				.body(ApiResponse.forbidden(messageService.get("error.forbidden.account-disabled")));
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
		String message = messageService.get("error.bad-request.invalid-parameter", ex.getName(), ex.getValue());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(ApiResponse.badRequest(message));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiResponse<Void>> handleNotReadable(HttpMessageNotReadableException ex) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(ApiResponse.badRequest(messageService.get("error.bad-request.invalid-body")));
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
		String message = messageService.get("error.method-not-allowed", ex.getMethod());
		return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
				.body(ApiResponse.of(405, message, null));
	}

	@ExceptionHandler(NoHandlerFoundException.class)
	public ResponseEntity<ApiResponse<Void>> handleNoHandlerFound(NoHandlerFoundException ex) {
		String message = messageService.get("error.endpoint-not-found", ex.getHttpMethod(), ex.getRequestURL());
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(ApiResponse.notFound(message));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<Object>> handleValidationException(MethodArgumentNotValidException ex) {
		List<String> errors = ex.getBindingResult()
				.getFieldErrors()
				.stream()
				.map(error -> error.getField() + ": " + error.getDefaultMessage())
				.collect(Collectors.toList());

		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(ApiResponse.badRequest(messageService.get("error.bad-request.validation"), errors));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleGeneral(Exception ex) {
		log.error("Unexpected exception: {}", ex.getMessage(), ex);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ApiResponse.error(messageService.get("error.internal")));
	}
}
