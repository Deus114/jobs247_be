package com.recruitment.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@EnableConfigurationProperties(FileUploadProperties.class)
public class S3Config {

	@Value("${aws.region}")
	private String region;

	@Value("${aws.access-key}")
	private String accessKey;

	@Value("${aws.secret-key}")
	private String secretKey;

	@Value("${aws.s3.bucket}")
	private String bucket;

	@Value("${aws.s3.public-base-url}")
	private String publicBaseUrl;

	@Bean
	S3Client s3Client() {
		return S3Client.builder()
				.region(Region.of(region))
				.credentialsProvider(StaticCredentialsProvider.create(
						AwsBasicCredentials.create(accessKey, secretKey)))
				.build();
	}

	@Bean
	S3Properties s3Properties() {
		return new S3Properties(bucket, normalizeBaseUrl(publicBaseUrl));
	}

	private String normalizeBaseUrl(String baseUrl) {
		if (baseUrl == null || baseUrl.isBlank()) {
			return "";
		}
		return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
	}

	public record S3Properties(String bucket, String publicBaseUrl) {
	}
}
