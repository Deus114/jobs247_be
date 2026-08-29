package com.recruitment.dto.request.admin;

import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

public record UpdateAdminUserRequest(
		@Size(max = 100, message = "{validation.username.max-length}")
		String username,

		@Size(max = 255, message = "{validation.name.max-length}")
		String name,

		Long roleId,

		Boolean active,

		@Schema(description = "Mật khẩu hiện tại của user (bắt buộc khi đổi mật khẩu)")
		String currentPassword,

		@Schema(description = "Mật khẩu mới (kèm currentPassword)")
		String newPassword,

		@Schema(type = "string", format = "binary", description = "Ảnh avatar (tùy chọn)")
		MultipartFile avatar) {
}
