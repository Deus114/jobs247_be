package com.recruitment.service;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.recruitment.dto.request.common.RegisterDeviceTokenRequest;
import com.recruitment.dto.request.common.UnregisterDeviceTokenRequest;
import com.recruitment.dto.response.common.ApiResponse;
import com.recruitment.entity.AdminUser;
import com.recruitment.entity.DeviceToken;
import com.recruitment.entity.User;
import com.recruitment.enums.DevicePlatformEnum;
import com.recruitment.enums.NotificationRecipientTypeEnum;
import com.recruitment.exception.AccountInactiveException;
import com.recruitment.exception.BusinessRuleException;
import com.recruitment.exception.InvalidTokenException;
import com.recruitment.exception.ResourceNotFoundException;
import com.recruitment.repository.AdminUserRepository;
import com.recruitment.repository.DeviceTokenRepository;
import com.recruitment.repository.UserRepository;
import com.recruitment.security.JwtTokenProvider;

@Service
public class DeviceTokenService {

	private final DeviceTokenRepository deviceTokenRepository;
	private final UserRepository userRepository;
	private final AdminUserRepository adminUserRepository;
	private final MessageService messageService;

	public DeviceTokenService(
			DeviceTokenRepository deviceTokenRepository,
			UserRepository userRepository,
			AdminUserRepository adminUserRepository,
			MessageService messageService) {
		this.deviceTokenRepository = deviceTokenRepository;
		this.userRepository = userRepository;
		this.adminUserRepository = adminUserRepository;
		this.messageService = messageService;
	}

	@Transactional
	public ResponseEntity<ApiResponse<Void>> registerForUser(RegisterDeviceTokenRequest request, Jwt jwt) {
		User user = requireUser(jwt);
		upsert(NotificationRecipientTypeEnum.USER.name(), user.getId(), request);
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(
				HttpStatus.CREATED.value(),
				messageService.get("device-token.register.success"),
				null));
	}

	@Transactional
	public ResponseEntity<ApiResponse<Void>> registerForAdmin(RegisterDeviceTokenRequest request, Jwt jwt) {
		AdminUser admin = requireAdmin(jwt);
		upsert(NotificationRecipientTypeEnum.ADMIN.name(), admin.getId(), request);
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(
				HttpStatus.CREATED.value(),
				messageService.get("device-token.register.success"),
				null));
	}

	@Transactional
	public ResponseEntity<ApiResponse<Void>> unregisterForUser(UnregisterDeviceTokenRequest request, Jwt jwt) {
		User user = requireUser(jwt);
		return unregister(request.token().trim(), NotificationRecipientTypeEnum.USER.name(), user.getId());
	}

	@Transactional
	public ResponseEntity<ApiResponse<Void>> unregisterForAdmin(UnregisterDeviceTokenRequest request, Jwt jwt) {
		AdminUser admin = requireAdmin(jwt);
		return unregister(request.token().trim(), NotificationRecipientTypeEnum.ADMIN.name(), admin.getId());
	}

	private void upsert(String ownerType, Long ownerId, RegisterDeviceTokenRequest request) {
		String tokenValue = request.token().trim();
		DeviceToken deviceToken = deviceTokenRepository.findByToken(tokenValue).orElseGet(DeviceToken::new);
		deviceToken.setOwnerType(ownerType);
		deviceToken.setOwnerId(ownerId);
		deviceToken.setToken(tokenValue);
		deviceToken.setPlatform(normalizePlatform(request.platform()));
		deviceToken.setDeviceInfo(trimToNull(request.deviceInfo()));
		deviceToken.setLastSeenAt(Instant.now());
		deviceTokenRepository.save(deviceToken);
	}

	@Transactional
	public void removeInvalidToken(String token) {
		deviceTokenRepository.deleteByToken(token);
	}

	private ResponseEntity<ApiResponse<Void>> unregister(String token, String ownerType, Long ownerId) {
		int deleted = deviceTokenRepository.deleteByTokenAndOwner(token, ownerType, ownerId);
		if (deleted == 0) {
			throw new ResourceNotFoundException("error.resource.device-token", "token", token);
		}
		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("device-token.unregister.success"),
				null));
	}

	private String normalizePlatform(String platform) {
		if (!StringUtils.hasText(platform)) {
			return null;
		}
		return DevicePlatformEnum.fromValue(platform)
				.orElseThrow(() -> new BusinessRuleException("error.device-token.platform.invalid", platform))
				.name();
	}

	private String trimToNull(String value) {
		if (!StringUtils.hasText(value)) {
			return null;
		}
		return value.trim();
	}

	private User requireUser(Jwt jwt) {
		if (jwt == null
				|| !JwtTokenProvider.USER_ACCOUNT.equals(jwt.getClaimAsString("account"))
				|| !JwtTokenProvider.USER_ACCESS_TYPE.equals(jwt.getClaimAsString("type"))) {
			throw new InvalidTokenException("error.unauthorized.invalid-token");
		}
		User user = userRepository.findByEmail(jwt.getSubject())
				.orElseThrow(() -> new InvalidTokenException("error.unauthorized.invalid-token"));
		if (!user.isActive()) {
			throw new AccountInactiveException("error.auth.account.inactive");
		}
		return user;
	}

	private AdminUser requireAdmin(Jwt jwt) {
		if (jwt == null || JwtTokenProvider.USER_ACCOUNT.equals(jwt.getClaimAsString("account"))) {
			throw new InvalidTokenException("error.unauthorized.invalid-token");
		}
		AdminUser admin = adminUserRepository.findByUsername(jwt.getSubject())
				.orElseThrow(() -> new InvalidTokenException("error.unauthorized.invalid-token"));
		if (!admin.isActive()) {
			throw new AccountInactiveException("error.auth.account.inactive");
		}
		return admin;
	}
}
