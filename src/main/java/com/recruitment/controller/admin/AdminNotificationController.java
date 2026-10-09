package com.recruitment.controller.admin;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.recruitment.config.OpenAPIConfig;
import com.recruitment.dto.request.common.NotificationFilterRequest;
import com.recruitment.dto.response.common.ApiResponse;
import com.recruitment.dto.response.common.NotificationResponse;
import com.recruitment.dto.response.common.PaginatedResponse;
import com.recruitment.dto.response.common.UnreadCountResponse;
import com.recruitment.service.NotificationInboxService;
import com.recruitment.util.constant.Constants;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping(Constants.ADMIN_NOTIFICATIONS_BASE_PATH)
@Tag(name = "Admin Notification")
@SecurityRequirement(name = OpenAPIConfig.BEARER_AUTH_SCHEME)
public class AdminNotificationController {

	private final NotificationInboxService notificationInboxService;

	public AdminNotificationController(NotificationInboxService notificationInboxService) {
		this.notificationInboxService = notificationInboxService;
	}

	@GetMapping
	public ResponseEntity<ApiResponse<PaginatedResponse<NotificationResponse>>> getList(
			@ParameterObject
			NotificationFilterRequest filter,
			@ParameterObject
			@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
			Pageable pageable,
			@Parameter(hidden = true)
			@AuthenticationPrincipal
			Jwt jwt) {
		return notificationInboxService.getAdminList(filter, pageable, jwt);
	}

	@GetMapping("/unread-count")
	public ResponseEntity<ApiResponse<UnreadCountResponse>> getUnreadCount(
			@Parameter(hidden = true)
			@AuthenticationPrincipal
			Jwt jwt) {
		return notificationInboxService.getAdminUnreadCount(jwt);
	}

	@PatchMapping("/{id}/read")
	public ResponseEntity<ApiResponse<NotificationResponse>> markRead(
			@PathVariable
			Long id,
			@Parameter(hidden = true)
			@AuthenticationPrincipal
			Jwt jwt) {
		return notificationInboxService.markAdminRead(id, jwt);
	}

	@PatchMapping("/read-all")
	public ResponseEntity<ApiResponse<Void>> markAllRead(
			@Parameter(hidden = true)
			@AuthenticationPrincipal
			Jwt jwt) {
		return notificationInboxService.markAdminAllRead(jwt);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<Void>> delete(
			@PathVariable
			Long id,
			@Parameter(hidden = true)
			@AuthenticationPrincipal
			Jwt jwt) {
		return notificationInboxService.deleteAdmin(id, jwt);
	}
}
