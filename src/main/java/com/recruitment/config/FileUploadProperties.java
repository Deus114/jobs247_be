package com.recruitment.config;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

@ConfigurationProperties(prefix = "file-upload")
public class FileUploadProperties {

	private long maxImageSizeBytes = 10 * 1024 * 1024;
	private String allowedImageContentTypes = "image/jpeg,image/png,image/webp,image/gif";
	private String allowedImageExtensions = "jpg,jpeg,png,webp,gif";

	public long getMaxImageSizeBytes() {
		return maxImageSizeBytes;
	}

	public void setMaxImageSizeBytes(long maxImageSizeBytes) {
		this.maxImageSizeBytes = maxImageSizeBytes;
	}

	public String getAllowedImageContentTypes() {
		return allowedImageContentTypes;
	}

	public void setAllowedImageContentTypes(String allowedImageContentTypes) {
		this.allowedImageContentTypes = allowedImageContentTypes;
	}

	public String getAllowedImageExtensions() {
		return allowedImageExtensions;
	}

	public void setAllowedImageExtensions(String allowedImageExtensions) {
		this.allowedImageExtensions = allowedImageExtensions;
	}

	public Set<String> allowedImageContentTypeSet() {
		return splitToSet(allowedImageContentTypes);
	}

	public Set<String> allowedImageExtensionSet() {
		return splitToSet(allowedImageExtensions);
	}

	private Set<String> splitToSet(String value) {
		if (!StringUtils.hasText(value)) {
			return Set.of();
		}
		return Arrays.stream(value.split(","))
				.filter(Objects::nonNull)
				.map(item -> item.trim().toLowerCase(Locale.ROOT))
				.filter(item -> !item.isEmpty())
				.collect(Collectors.toSet());
	}
}
