package com.recruitment.service;

import java.io.IOException;
import java.util.Locale;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.recruitment.config.FileUploadProperties;
import com.recruitment.dto.response.common.UploadedFileResponse;
import com.recruitment.enums.UploadFolderType;
import com.recruitment.exception.FileValidationException;
import com.recruitment.exception.StorageException;

@Service
public class FileStorageService {

	private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);
	private static final String UPLOADS_PREFIX = "uploads";

	private final S3StorageService s3StorageService;
	private final FileUploadProperties fileUploadProperties;

	public FileStorageService(S3StorageService s3StorageService, FileUploadProperties fileUploadProperties) {
		this.s3StorageService = s3StorageService;
		this.fileUploadProperties = fileUploadProperties;
	}

	public UploadedFileResponse uploadImage(MultipartFile file, UploadFolderType folderType) {
		validateImage(file);

		String extension = extractExtension(file.getOriginalFilename());
		String safeFileName = buildSafeFileName(extension);
		String key = UPLOADS_PREFIX + "/" + folderType.getFolder() + "/" + safeFileName;
		String contentType = normalizeContentType(file.getContentType());

		try {
			s3StorageService.putObject(key, file.getBytes(), contentType);
		} catch (IOException ex) {
			log.error("Failed to read multipart file bytes", ex);
			throw new StorageException("error.file.upload.failed", ex);
		}

		return new UploadedFileResponse(safeFileName, key, s3StorageService.toPublicUrl(key));
	}

	public UploadedFileResponse uploadDocument(MultipartFile file, UploadFolderType folderType) {
		validateDocument(file);

		String extension = extractExtension(file.getOriginalFilename());
		String safeFileName = buildSafeFileName(extension);
		String key = UPLOADS_PREFIX + "/" + folderType.getFolder() + "/" + safeFileName;
		String contentType = normalizeContentType(file.getContentType());

		try {
			s3StorageService.putObject(key, file.getBytes(), contentType);
		} catch (IOException ex) {
			log.error("Failed to read multipart file bytes", ex);
			throw new StorageException("error.file.upload.failed", ex);
		}

		return new UploadedFileResponse(safeFileName, key, s3StorageService.toPublicUrl(key));
	}

	public void deleteByPublicUrl(String publicUrl) {
		s3StorageService.toObjectKey(publicUrl).ifPresent(key -> {
			try {
				s3StorageService.deleteObject(key);
			} catch (StorageException ex) {
				log.warn("Skip S3 delete failure for url={}", publicUrl);
			}
		});
	}

	private void validateImage(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new FileValidationException("error.file.empty");
		}

		if (file.getSize() > fileUploadProperties.getMaxImageSizeBytes()) {
			throw new FileValidationException(
					"error.file.too-large",
					fileUploadProperties.getMaxImageSizeBytes() / (1024 * 1024));
		}

		String contentType = normalizeContentType(file.getContentType());
		if (!fileUploadProperties.allowedImageContentTypeSet().contains(contentType)) {
			throw new FileValidationException("error.file.invalid-type");
		}

		String extension = extractExtension(file.getOriginalFilename());
		if (!fileUploadProperties.allowedImageExtensionSet().contains(extension)) {
			throw new FileValidationException("error.file.invalid-extension");
		}
	}

	private void validateDocument(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new FileValidationException("error.file.empty");
		}
		if (file.getSize() > fileUploadProperties.getMaxDocumentSizeBytes()) {
			throw new FileValidationException(
					"error.file.too-large",
					fileUploadProperties.getMaxDocumentSizeBytes() / (1024 * 1024));
		}
		String contentType = normalizeContentType(file.getContentType());
		if (!fileUploadProperties.allowedDocumentContentTypeSet().contains(contentType)) {
			throw new FileValidationException("error.file.invalid-type");
		}
		String extension = extractExtension(file.getOriginalFilename());
		if (!fileUploadProperties.allowedDocumentExtensionSet().contains(extension)) {
			throw new FileValidationException("error.file.invalid-extension");
		}
	}

	private String buildSafeFileName(String extension) {
		return System.currentTimeMillis() + "-" + UUID.randomUUID().toString().replace("-", "") + "." + extension;
	}

	private String extractExtension(String originalFilename) {
		String filename = StringUtils.cleanPath(originalFilename == null ? "" : originalFilename);
		int dotIndex = filename.lastIndexOf('.');
		if (dotIndex < 0 || dotIndex == filename.length() - 1) {
			throw new FileValidationException("error.file.invalid-extension");
		}
		return filename.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
	}

	private String normalizeContentType(String contentType) {
		if (contentType == null) {
			return "";
		}
		return contentType.trim().toLowerCase(Locale.ROOT);
	}
}
