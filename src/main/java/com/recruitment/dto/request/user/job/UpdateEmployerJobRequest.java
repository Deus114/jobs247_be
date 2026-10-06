package com.recruitment.dto.request.user.job;

import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record UpdateEmployerJobRequest(
		@Size(max = 255, message = "{validation.job.title.max-length}") String title,

		@Schema(description = "Thay toàn bộ danh sách ngành nghề; không gửi = giữ nguyên") List<Long> industryIds,

		@Schema(description = "Thay toàn bộ trình độ; [] = không yêu cầu trình độ; không gửi = giữ nguyên") List<Long> educationLevelIds,

		@Schema(description = "Thay toàn bộ danh sách địa điểm; không gửi = giữ nguyên") List<Long> provinceIds,

		@Schema(description = "Thay toàn bộ loại hình làm việc; không gửi = giữ nguyên") List<String> employmentTypes,

		@Schema(description = "Thay toàn bộ kinh nghiệm; [] = không yêu cầu; không gửi = giữ nguyên") List<String> experienceLevels,

		@Min(value = 0, message = "{validation.job.experience-years.min}") @Max(value = 50, message = "{validation.job.experience-years.max}") @Schema(description = "Số năm kinh nghiệm; không gửi = giữ nguyên") Integer experienceYears,

		Integer salaryMin,

		Integer salaryMax,  Boolean salaryNegotiable,

		LocalDate deadline,

		@Schema(description = "Pass empty string to clear. Địa điểm làm việc (text tự do)") String workLocation,

		@Schema(description = "Pass empty string to clear. Thời gian làm việc (text tự do)") String workingTime,

		String description,

		String requirements,

		String benefits,

		@Size(max = 1000, message = "{validation.job.applicant-question.max-length}") @Schema(description = "Pass empty string to clear") String applicantQuestion) {
}
