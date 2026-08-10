package com.recruitment.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "statusCode", "message", "data" })
public class ApiResponse<T> {

	private int statusCode;
	private String message;
	private T data;

	public ApiResponse() {
	}

	private ApiResponse(int statusCode, String message, T data) {
		this.statusCode = statusCode;
		this.message = message;
		this.data = data;
	}

	public static <T> ApiResponse<T> success(T data) {
		return new ApiResponse<>(200, "Success", data);
	}

	public static <T> ApiResponse<T> success(String message, T data) {
		return new ApiResponse<>(200, message, data);
	}

	public static <T> ApiResponse<T> of(int statusCode, String message, T data) {
		return new ApiResponse<>(statusCode, message, data);
	}

	public static <T> ApiResponse<T> notFound(String message) {
		return new ApiResponse<>(404, message, null);
	}

	public static <T> ApiResponse<T> badRequest(String message) {
		return new ApiResponse<>(400, message, null);
	}

	public static ApiResponse<Object> badRequest(String message, Object details) {
		return new ApiResponse<>(400, message, details);
	}

	public static <T> ApiResponse<T> unauthorized(String message) {
		return new ApiResponse<>(401, message, null);
	}

	public static <T> ApiResponse<T> forbidden(String message) {
		return new ApiResponse<>(403, message, null);
	}

	public static <T> ApiResponse<T> error(String message) {
		return new ApiResponse<>(500, message, null);
	}

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}
}
