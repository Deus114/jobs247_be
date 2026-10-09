package com.recruitment.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;

@Configuration
@ConditionalOnProperty(name = "app.fcm.enabled", havingValue = "true")
public class FirebaseConfig {

	@Bean
	FirebaseMessaging firebaseMessaging(
			@Value("${app.fcm.credentials-path:}")
			String credentialsPath) throws IOException {
		if (!StringUtils.hasText(credentialsPath)) {
			throw new IllegalStateException("FIREBASE_CREDENTIALS_PATH is required when FCM is enabled");
		}
		Path path = Path.of(credentialsPath.trim());
		if (!Files.isRegularFile(path)) {
			throw new IllegalStateException("Firebase credentials file not found: " + path);
		}
		try (InputStream input = Files.newInputStream(path)) {
			FirebaseOptions options = FirebaseOptions.builder()
					.setCredentials(GoogleCredentials.fromStream(input))
					.build();
			FirebaseApp app = FirebaseApp.getApps().isEmpty()
					? FirebaseApp.initializeApp(options)
					: FirebaseApp.getInstance();
			return FirebaseMessaging.getInstance(app);
		}
	}
}
