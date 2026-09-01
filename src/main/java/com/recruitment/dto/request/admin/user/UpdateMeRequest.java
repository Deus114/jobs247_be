package com.recruitment.dto.request.admin.user;

import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

public record UpdateMeRequest(
		@Size(max = 255, message = "{validation.name.max-length}")
		String name,

		@Schema(description = "Mật khẩu hiện tại (bắt buộc khi đổi mật khẩu)")
		String currentPassword,

		@Schema(description = "Mật khẩu mới (kèm currentPassword)")
		String newPassword,

		@Schema(type = "string", format = "binary", description = "Ảnh avatar (tùy chọn)")
		MultipartFile avatar) {
}
