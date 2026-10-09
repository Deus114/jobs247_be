package com.recruitment.service;

import java.time.Instant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.recruitment.dto.request.common.NotificationFilterRequest;
import com.recruitment.dto.response.common.ApiResponse;
import com.recruitment.dto.response.common.NotificationResponse;
import com.recruitment.dto.response.common.PaginatedResponse;
import com.recruitment.dto.response.common.UnreadCountResponse;
import com.recruitment.entity.AdminUser;
import com.recruitment.entity.Notification;
import com.recruitment.entity.User;
import com.recruitment.enums.NotificationRecipientTypeEnum;
import com.recruitment.exception.AccountInactiveException;
import com.recruitment.exception.InvalidTokenException;
import com.recruitment.exception.ResourceNotFoundException;
import com.recruitment.mapper.NotificationMapper;
import com.recruitment.repository.AdminUserRepository;
import com.recruitment.repository.NotificationRepository;
import com.recruitment.repository.UserRepository;
import com.recruitment.security.JwtTokenProvider;
import com.recruitment.specification.NotificationSpecification;

@Service
public class NotificationInboxService {

	private final NotificationRepository notificationRepository;
	private final NotificationMapper notificationMapper;
	private final UserRepository userRepository;
	private final AdminUserRepository adminUserRepository;
	private final MessageService messageService;

	public NotificationInboxService(
			NotificationRepository notificationRepository,
			NotificationMapper notificationMapper,
			UserRepository userRepository,
			AdminUserRepository adminUserRepository,
			MessageService messageService) {
		this.notificationRepository = notificationRepository;
		this.notificationMapper = notificationMapper;
		this.userRepository = userRepository;
		this.adminUserRepository = adminUserRepository;
		this.messageService = messageService;
	}

	@Transactional(readOnly = true)
	public ResponseEntity<ApiResponse<PaginatedResponse<NotificationResponse>>> getUserList(
			NotificationFilterRequest filter,
			Pageable pageable,
			Jwt jwt) {
		User user = requireUser(jwt);
		return list(NotificationRecipientTypeEnum.USER.name(), user.getId(), filter, pageable);
	}

	@Transactional(readOnly = true)
	public ResponseEntity<ApiResponse<PaginatedResponse<NotificationResponse>>> getAdminList(
			NotificationFilterRequest filter,
			Pageable pageable,
			Jwt jwt) {
		AdminUser admin = requireAdmin(jwt);
		return list(NotificationRecipientTypeEnum.ADMIN.name(), admin.getId(), filter, pageable);
	}

	@Transactional(readOnly = true)
	public ResponseEntity<ApiResponse<UnreadCountResponse>> getUserUnreadCount(Jwt jwt) {
		User user = requireUser(jwt);
		return unreadCount(NotificationRecipientTypeEnum.USER.name(), user.getId());
	}

	@Transactional(readOnly = true)
	public ResponseEntity<ApiResponse<UnreadCountResponse>> getAdminUnreadCount(Jwt jwt) {
		AdminUser admin = requireAdmin(jwt);
		return unreadCount(NotificationRecipientTypeEnum.ADMIN.name(), admin.getId());
	}

	@Transactional
	public ResponseEntity<ApiResponse<NotificationResponse>> markUserRead(Long id, Jwt jwt) {
		User user = requireUser(jwt);
		return markRead(id, NotificationRecipientTypeEnum.USER.name(), user.getId(), user.getEmail());
	}

	@Transactional
	public ResponseEntity<ApiResponse<NotificationResponse>> markAdminRead(Long id, Jwt jwt) {
		AdminUser admin = requireAdmin(jwt);
		return markRead(id, NotificationRecipientTypeEnum.ADMIN.name(), admin.getId(), admin.getUsername());
	}

	@Transactional
	public ResponseEntity<ApiResponse<Void>> markUserAllRead(Jwt jwt) {
		User user = requireUser(jwt);
		return markAllRead(NotificationRecipientTypeEnum.USER.name(), user.getId(), user.getEmail());
	}

	@Transactional
	public ResponseEntity<ApiResponse<Void>> markAdminAllRead(Jwt jwt) {
		AdminUser admin = requireAdmin(jwt);
		return markAllRead(NotificationRecipientTypeEnum.ADMIN.name(), admin.getId(), admin.getUsername());
	}

	@Transactional
	public ResponseEntity<ApiResponse<Void>> deleteUser(Long id, Jwt jwt) {
		User user = requireUser(jwt);
		return softDelete(id, NotificationRecipientTypeEnum.USER.name(), user.getId(), user.getEmail());
	}

	@Transactional
	public ResponseEntity<ApiResponse<Void>> deleteAdmin(Long id, Jwt jwt) {
		AdminUser admin = requireAdmin(jwt);
		return softDelete(id, NotificationRecipientTypeEnum.ADMIN.name(), admin.getId(), admin.getUsername());
	}

	private ResponseEntity<ApiResponse<PaginatedResponse<NotificationResponse>>> list(
			String recipientType,
			Long recipientId,
			NotificationFilterRequest filter,
			Pageable pageable) {
		Boolean unreadOnly = filter == null ? null : filter.unreadOnly();
		Page<NotificationResponse> page = notificationRepository
				.findAll(NotificationSpecification.forRecipient(recipientType, recipientId, unreadOnly), pageable)
				.map(notificationMapper::toResponse);
		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("notification.list.success"),
				PaginatedResponse.fromPage(page)));
	}

	private ResponseEntity<ApiResponse<UnreadCountResponse>> unreadCount(String recipientType, Long recipientId) {
		long count = notificationRepository.countByRecipientTypeAndRecipientIdAndReadAtIsNull(
				recipientType, recipientId);
		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("notification.unread-count.success"),
				new UnreadCountResponse(count)));
	}

	private ResponseEntity<ApiResponse<NotificationResponse>> markRead(
			Long id,
			String recipientType,
			Long recipientId,
			String actor) {
		Notification notification = notificationRepository
				.findByIdAndRecipientTypeAndRecipientId(id, recipientType, recipientId)
				.orElseThrow(() -> new ResourceNotFoundException("error.resource.notification", "id", id));
		if (notification.getReadAt() == null) {
			notification.setReadAt(Instant.now());
			notification.setUpdatedBy(actor);
			notification = notificationRepository.save(notification);
		}
		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("notification.read.success"),
				notificationMapper.toResponse(notification)));
	}

	private ResponseEntity<ApiResponse<Void>> markAllRead(String recipientType, Long recipientId, String actor) {
		notificationRepository.markAllRead(recipientType, recipientId, Instant.now(), actor);
		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("notification.read-all.success"),
				null));
	}

	private ResponseEntity<ApiResponse<Void>> softDelete(
			Long id,
			String recipientType,
			Long recipientId,
			String actor) {
		Notification notification = notificationRepository
				.findByIdAndRecipientTypeAndRecipientId(id, recipientType, recipientId)
				.orElseThrow(() -> new ResourceNotFoundException("error.resource.notification", "id", id));
		notification.setDeletedBy(actor);
		notificationRepository.save(notification);
		notificationRepository.delete(notification);
		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("notification.delete.success"),
				null));
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
