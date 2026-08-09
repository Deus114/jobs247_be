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

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(InvalidTokenException.class)
	public ResponseEntity<ApiResponse<Void>> handleInvalidToken(InvalidTokenException ex) {
		log.warn("InvalidTokenException: {}", ex.getMessage());
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(ApiResponse.unauthorized(ex.getMessage()));
	}

	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<ApiResponse<Void>> handleBadCredentials(BadCredentialsException ex) {
		log.warn("BadCredentialsException: {}", ex.getMessage());
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(ApiResponse.unauthorized("Email hoặc mật khẩu không đúng"));
	}

	@ExceptionHandler(DisabledException.class)
	public ResponseEntity<ApiResponse<Void>> handleDisabled(DisabledException ex) {
		log.warn("DisabledException: {}", ex.getMessage());
		return ResponseEntity.status(HttpStatus.FORBIDDEN)
				.body(ApiResponse.forbidden("Tài khoản admin đã bị vô hiệu hóa"));
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(ResourceNotFoundException ex) {
		log.warn("ResourceNotFoundException: {}", ex.getMessage());
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(ApiResponse.notFound(ex.getMessage()));
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
		String message = String.format("Tham số '%s' có giá trị '%s' không hợp lệ",
				ex.getName(), ex.getValue());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(ApiResponse.badRequest(message));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiResponse<Void>> handleNotReadable(HttpMessageNotReadableException ex) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(ApiResponse.badRequest("Request body không hợp lệ hoặc bị thiếu"));
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
		String message = String.format("Phương thức HTTP '%s' không được hỗ trợ cho endpoint này", ex.getMethod());
		return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
				.body(ApiResponse.ofError(405, message, "Method Not Allowed"));
	}

	@ExceptionHandler(NoHandlerFoundException.class)
	public ResponseEntity<ApiResponse<Void>> handleNoHandlerFound(NoHandlerFoundException ex) {
		String message = String.format("Không tìm thấy endpoint '%s %s'", ex.getHttpMethod(), ex.getRequestURL());
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
				.body(ApiResponse.badRequest("Dữ liệu không hợp lệ", errors));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleGeneral(Exception ex) {
		log.error("Unexpected exception: {}", ex.getMessage(), ex);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ApiResponse.error("Đã xảy ra lỗi hệ thống, vui lòng thử lại sau"));
	}
}
