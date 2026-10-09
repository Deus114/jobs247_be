package com.recruitment.dto.request.common;

import java.util.List;

import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import io.swagger.v3.oas.annotations.media.Schema;

public record PublicJobFilterRequest(
		String keyword,
		Long companyId,

		@Schema(description = "Một hoặc nhiều nhóm ngành — ?industryGroupIds=1&industryGroupIds=2") List<Long> industryGroupIds,

		@Schema(description = "Một hoặc nhiều ngành nghề — ?industryIds=1&industryIds=2") List<Long> industryIds,

		@Schema(description = "Một hoặc nhiều địa điểm — ?provinceIds=1&provinceIds=2") List<Long> provinceIds,

		@Schema(description = "Một hoặc nhiều trình độ — ?educationLevelIds=1&educationLevelIds=2") List<Long> educationLevelIds,

		@Schema(description = "Một hoặc nhiều hình thức — ?employmentTypes=FULL_TIME&employmentTypes=PART_TIME") List<String> employmentTypes,

		@Schema(description = "Một hoặc nhiều mức kinh nghiệm — ?experienceLevels=SENIOR&experienceLevels=LEAD") List<String> experienceLevels,

		Boolean hot,

		@Schema(description = "Lương từ, cùng đơn vị với salaryMin/salaryMax. Bỏ trống nếu không giới hạn đầu dưới.") Integer salaryFrom,

		@Schema(description = "Lương đến, cùng đơn vị với salaryMin/salaryMax. Bỏ trống nếu không giới hạn đầu trên.") Integer salaryTo) {

	/** Chỉ cache Redis khi hot=true và không kèm filter khác. */
	public boolean isHotOnlyListing() {
		return Boolean.TRUE.equals(hot)
				&& !StringUtils.hasText(keyword)
				&& companyId == null
				&& CollectionUtils.isEmpty(industryGroupIds)
				&& CollectionUtils.isEmpty(industryIds)
				&& CollectionUtils.isEmpty(provinceIds)
				&& CollectionUtils.isEmpty(educationLevelIds)
				&& CollectionUtils.isEmpty(employmentTypes)
				&& CollectionUtils.isEmpty(experienceLevels)
				&& salaryFrom == null
				&& salaryTo == null;
	}
}
