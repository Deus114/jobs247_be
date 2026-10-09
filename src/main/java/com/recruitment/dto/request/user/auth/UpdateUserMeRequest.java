package com.recruitment.dto.request.user.auth;

import jakarta.validation.constraints.Size;

import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.media.Schema;

public record UpdateUserMeRequest(
		@Size(max = 255, message = "{validation.name.max-length}") String name,

		@Size(max = 20, message = "{validation.company.phone.max-length}") String phone,

		@Schema(description = "Bật hoặc tắt nhận thông báo đẩy. Bỏ trống nếu không đổi") Boolean pushEnabled,

		@Schema(description = "Mật khẩu hiện tại (bắt buộc khi đổi mật khẩu)") String currentPassword,

		@Schema(description = "Mật khẩu mới (kèm currentPassword)") String newPassword,

		@Schema(type = "string", format = "binary", description = "Ảnh avatar (tùy chọn)") MultipartFile avatar) {
}
