package com.recruitment.dto.response.common;

public record UploadedFileResponse(
		String fileName,
		String path,
		String url) {
}
