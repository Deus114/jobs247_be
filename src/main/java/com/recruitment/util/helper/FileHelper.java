package com.recruitment.util.helper;

import org.springframework.web.multipart.MultipartFile;

public final class FileHelper {

	private FileHelper() {
	}

	public static boolean hasFile(MultipartFile file) {
		return file != null && !file.isEmpty();
	}
}
