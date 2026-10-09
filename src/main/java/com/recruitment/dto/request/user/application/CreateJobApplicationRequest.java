package com.recruitment.dto.request.user.application;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.media.Schema;

public record CreateJobApplicationRequest(
		@NotBlank(message = "{validation.name.required}")
		@Size(max = 255, message = "{validation.name.max-length}")
		String fullName,

		@NotBlank(message = "{validation.email.required}")
		@Email(message = "{validation.email.invalid}")
		@Size(max = 255, message = "{validation.email.max-length}")
		String email,

		@Size(max = 20, message = "{validation.company.phone.max-length}")
		String phone,

		@Size(max = 500, message = "{validation.application.cover-letter.max-length}")
		String coverLetter,

		@NotNull(message = "{validation.application.cv.required}")
		@Schema(type = "string", format = "binary", description = "CV (PDF/DOC/DOCX)")
		MultipartFile cvFile) {
}
