package com.recruitment.dto.request.user.job;

import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateEmployerJobRequest(
		@NotNull(message = "{validation.job.company.required}") Long companyId,

		@NotBlank(message = "{validation.job.title.required}") @Size(max = 255, message = "{validation.job.title.max-length}") String title,

		@NotEmpty(message = "{validation.job.industries.required}") List<Long> industryIds,

		@Schema(description = "Tùy chọn — một hoặc nhiều trình độ; bỏ qua hoặc [] nếu không yêu cầu") List<Long> educationLevelIds,

		@NotEmpty(message = "{validation.job.provinces.required}") List<Long> provinceIds,

		@NotEmpty(message = "{validation.job.employment-types.required}") @Schema(description = "FULL_TIME, PART_TIME, INTERNSHIP, CONTRACT, FREELANCE — chọn một hoặc nhiều") List<String> employmentTypes,

		@Schema(description = "Tùy chọn — INTERN, FRESHER, JUNIOR, MIDDLE, SENIOR, LEAD, MANAGER; bỏ qua hoặc [] nếu không yêu cầu") List<String> experienceLevels,

		@Min(value = 0, message = "{validation.job.experience-years.min}") @Max(value = 50, message = "{validation.job.experience-years.max}") @Schema(description = "Số năm kinh nghiệm yêu cầu (tùy chọn)") Integer experienceYears,

		Integer salaryMin,

		Integer salaryMax,

		Boolean salaryNegotiable,

		@NotNull(message = "{validation.job.deadline.required}") LocalDate deadline,

		@Schema(description = "Địa điểm làm việc") String workLocation,

		@Schema(description = "Thời gian làm việc") String workingTime,

		@NotBlank(message = "{validation.job.description.required}") String description,

		@NotBlank(message = "{validation.job.requirements.required}") String requirements,

		@NotBlank(message = "{validation.job.benefits.required}") String benefits,

		@Size(max = 1000, message = "{validation.job.applicant-question.max-length}") @Schema(description = "Câu hỏi dành cho ứng viên (hiển thị ở form thư xin việc)") String applicantQuestion) {
}
