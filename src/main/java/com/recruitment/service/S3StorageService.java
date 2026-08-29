package com.recruitment.service;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.recruitment.config.S3Config.S3Properties;
import com.recruitment.exception.StorageException;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class S3StorageService {

	private static final Logger log = LoggerFactory.getLogger(S3StorageService.class);

	private final S3Client s3Client;
	private final S3Properties s3Properties;

	public S3StorageService(S3Client s3Client, S3Properties s3Properties) {
		this.s3Client = s3Client;
		this.s3Properties = s3Properties;
	}

	public void putObject(String key, byte[] content, String contentType) {
		try {
			PutObjectRequest request = PutObjectRequest.builder()
					.bucket(s3Properties.bucket())
					.key(key)
					.contentType(contentType)
					.build();
			s3Client.putObject(request, RequestBody.fromBytes(content));
		} catch (Exception ex) {
			log.error("Failed to upload object to S3: key={}", key, ex);
			throw new StorageException("error.file.upload.failed", ex);
		}
	}

	public void deleteObject(String key) {
		try {
			DeleteObjectRequest request = DeleteObjectRequest.builder()
					.bucket(s3Properties.bucket())
					.key(key)
					.build();
			s3Client.deleteObject(request);
		} catch (Exception ex) {
			log.warn("Failed to delete object from S3: key={}", key, ex);
			throw new StorageException("error.file.delete.failed", ex);
		}
	}

	public String toPublicUrl(String key) {
		return s3Properties.publicBaseUrl() + "/" + key;
	}

	public Optional<String> toObjectKey(String publicUrl) {
		if (publicUrl == null || publicUrl.isBlank()) {
			return Optional.empty();
		}

		String baseUrl = s3Properties.publicBaseUrl();
		if (baseUrl == null || baseUrl.isBlank()) {
			return Optional.empty();
		}

		String prefix = baseUrl + "/";
		if (!publicUrl.startsWith(prefix)) {
			return Optional.empty();
		}

		String key = publicUrl.substring(prefix.length()).trim();
		if (key.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(key);
	}
}
