package com.recruitment.specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.recruitment.dto.request.common.PublicCompanyFilterRequest;
import com.recruitment.dto.request.common.PublicJobFilterRequest;
import com.recruitment.entity.Company;
import com.recruitment.entity.Job;
import com.recruitment.enums.CompanyStatusEnum;
import com.recruitment.enums.JobStatusEnum;
import com.recruitment.util.helper.AccentHelper;
import com.recruitment.util.helper.SearchHelper;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

public final class PublicCatalogSpecification {

	private PublicCatalogSpecification() {
	}

	public static Specification<Job> buildJobs(PublicJobFilterRequest filter) {
		return (root, query, criteriaBuilder) -> {
			if (query != null) {
				query.distinct(true);
			}
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(root.get("status"), JobStatusEnum.APPROVED.name()),
					criteriaBuilder.isTrue(root.get("active")),
					criteriaBuilder.greaterThanOrEqualTo(root.get("deadline"), LocalDate.now()),
					criteriaBuilder.equal(root.get("company").get("status"), CompanyStatusEnum.APPROVED.name()),
					criteriaBuilder.isTrue(root.get("company").get("active")));

			String keyword = AccentHelper.normalizeForSearch(filter.keyword());
			if (keyword != null) {
				predicate = criteriaBuilder.and(predicate, criteriaBuilder.or(
						SearchHelper.likeUnaccent(criteriaBuilder, root.get("title"), keyword),
						SearchHelper.likeUnaccent(criteriaBuilder, root.get("company").get("name"), keyword)));
			}
			if (filter.companyId() != null) {
				predicate = criteriaBuilder.and(predicate,
						criteriaBuilder.equal(root.get("company").get("id"), filter.companyId()));
			}

			boolean filterByIndustryGroup = hasIds(filter.industryGroupIds());
			boolean filterByIndustry = hasIds(filter.industryIds());
			if (filterByIndustryGroup || filterByIndustry) {
				Join<?, ?> industries = root.join("industries");
				if (filterByIndustryGroup) {
					predicate = criteriaBuilder.and(predicate,
							industries.get("industryGroup").get("id").in(filter.industryGroupIds()));
				}
				if (filterByIndustry) {
					predicate = criteriaBuilder.and(predicate,
							industries.get("id").in(filter.industryIds()));
				}
			}
			if (hasIds(filter.provinceIds())) {
				Join<?, ?> provinces = root.join("provinces");
				predicate = criteriaBuilder.and(predicate,
						provinces.get("id").in(filter.provinceIds()));
			}
			if (hasIds(filter.educationLevelIds())) {
				Join<?, ?> educationLevels = root.join("educationLevels");
				predicate = criteriaBuilder.and(predicate,
						educationLevels.get("id").in(filter.educationLevelIds()));
			}

			List<String> employmentTypes = normalizeEnums(filter.employmentTypes());
			if (!employmentTypes.isEmpty()) {
				Join<?, ?> types = root.join("employmentTypes");
				predicate = criteriaBuilder.and(predicate, types.in(employmentTypes));
			}
			List<String> experienceLevels = normalizeEnums(filter.experienceLevels());
			if (!experienceLevels.isEmpty()) {
				Join<?, ?> levels = root.join("experienceLevels");
				predicate = criteriaBuilder.and(predicate, levels.in(experienceLevels));
			}
			if (filter.hot() != null) {
				predicate = criteriaBuilder.and(predicate,
						criteriaBuilder.equal(root.get("hot"), filter.hot()));
			}
			predicate = criteriaBuilder.and(predicate,
					JobSpecification.salaryOverlaps(
							criteriaBuilder, root, filter.salaryFrom(), filter.salaryTo()));
			return predicate;
		};
	}

	public static Specification<Job> relatedJobs(Job source) {
		List<Long> industryIds = source.getIndustries().stream().map(industry -> industry.getId()).toList();
		List<Long> provinceIds = source.getProvinces().stream().map(province -> province.getId()).toList();
		return recommendedJobs(industryIds, provinceIds, List.of(source.getId()));
	}

	/**
	 * Việc public còn hạn, cùng ngành hoặc địa điểm với lịch sử xem; loại trừ id đã xem.
	 */
	public static Specification<Job> recommendedJobs(
			List<Long> industryIds,
			List<Long> provinceIds,
			List<Long> excludeJobIds) {
		return (root, query, criteriaBuilder) -> {
			if (query != null) {
				query.distinct(true);
			}
			Predicate base = publicVisiblePredicate(root, criteriaBuilder, excludeJobIds);

			boolean hasIndustry = hasIds(industryIds);
			boolean hasProvince = hasIds(provinceIds);
			if (!hasIndustry && !hasProvince) {
				return criteriaBuilder.and(base, criteriaBuilder.disjunction());
			}

			Predicate related = criteriaBuilder.disjunction();
			if (hasIndustry) {
				Join<?, ?> industries = root.join("industries");
				related = criteriaBuilder.or(related, industries.get("id").in(industryIds));
			}
			if (hasProvince) {
				Join<?, ?> provinces = root.join("provinces");
				related = criteriaBuilder.or(related, provinces.get("id").in(provinceIds));
			}
			return criteriaBuilder.and(base, related);
		};
	}

	/**
	 * Việc public còn hạn dùng để bù size. Chỉ loại tin thuộc phần gợi ý
	 * (khớp ngành/địa điểm và không nằm trong lịch sử xem); tin đã xem vẫn được lấy.
	 */
	public static Specification<Job> latestPublicJobs(
			List<Long> industryIds,
			List<Long> provinceIds,
			List<Long> viewedJobIds) {
		return (root, query, criteriaBuilder) -> {
			Predicate base = publicVisiblePredicate(root, criteriaBuilder, List.of());
			boolean hasIndustry = hasIds(industryIds);
			boolean hasProvince = hasIds(provinceIds);
			if (!hasIndustry && !hasProvince) {
				return base;
			}

			Predicate related = criteriaBuilder.disjunction();
			if (hasIndustry) {
				Subquery<Long> industryMatch = query.subquery(Long.class);
				Root<Job> jobRoot = industryMatch.from(Job.class);
				Join<?, ?> industries = jobRoot.join("industries");
				industryMatch.select(jobRoot.get("id"));
				industryMatch.where(
						criteriaBuilder.equal(jobRoot.get("id"), root.get("id")),
						industries.get("id").in(industryIds));
				related = criteriaBuilder.or(related, criteriaBuilder.exists(industryMatch));
			}
			if (hasProvince) {
				Subquery<Long> provinceMatch = query.subquery(Long.class);
				Root<Job> jobRoot = provinceMatch.from(Job.class);
				Join<?, ?> provinces = jobRoot.join("provinces");
				provinceMatch.select(jobRoot.get("id"));
				provinceMatch.where(
						criteriaBuilder.equal(jobRoot.get("id"), root.get("id")),
						provinces.get("id").in(provinceIds));
				related = criteriaBuilder.or(related, criteriaBuilder.exists(provinceMatch));
			}

			Predicate inRecommended = related;
			if (!CollectionUtils.isEmpty(viewedJobIds)) {
				inRecommended = criteriaBuilder.and(
						related,
						criteriaBuilder.not(root.get("id").in(viewedJobIds)));
			}
			return criteriaBuilder.and(base, criteriaBuilder.not(inRecommended));
		};
	}

	private static Predicate publicVisiblePredicate(
			Root<Job> root,
			CriteriaBuilder criteriaBuilder,
			List<Long> excludeJobIds) {
		Predicate base = criteriaBuilder.and(
				criteriaBuilder.equal(root.get("status"), JobStatusEnum.APPROVED.name()),
				criteriaBuilder.isTrue(root.get("active")),
				criteriaBuilder.greaterThanOrEqualTo(root.get("deadline"), LocalDate.now()),
				criteriaBuilder.equal(root.get("company").get("status"), CompanyStatusEnum.APPROVED.name()),
				criteriaBuilder.isTrue(root.get("company").get("active")));
		if (!CollectionUtils.isEmpty(excludeJobIds)) {
			base = criteriaBuilder.and(base, criteriaBuilder.not(root.get("id").in(excludeJobIds)));
		}
		return base;
	}

	public static Specification<Company> buildCompanies(PublicCompanyFilterRequest filter) {
		return (root, query, criteriaBuilder) -> {
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(root.get("status"), CompanyStatusEnum.APPROVED.name()),
					criteriaBuilder.isTrue(root.get("active")));

			String keyword = AccentHelper.normalizeForSearch(filter.keyword());
			if (keyword != null) {
				predicate = criteriaBuilder.and(predicate,
						SearchHelper.likeUnaccent(criteriaBuilder, root.get("name"), keyword));
			}
			if (filter.provinceId() != null) {
				predicate = criteriaBuilder.and(predicate,
						criteriaBuilder.equal(root.get("province").get("id"), filter.provinceId()));
			}
			if (StringUtils.hasText(filter.companySize())) {
				predicate = criteriaBuilder.and(predicate,
						criteriaBuilder.equal(root.get("size"), filter.companySize().trim().toUpperCase()));
			}
			if (filter.industryId() != null) {
				if (query != null) {
					query.distinct(true);
				}
				Join<?, ?> industries = root.join("industries");
				predicate = criteriaBuilder.and(predicate,
						criteriaBuilder.equal(industries.get("id"), filter.industryId()));
			}
			return predicate;
		};
	}

	private static boolean hasIds(List<Long> ids) {
		return !CollectionUtils.isEmpty(ids);
	}

	private static List<String> normalizeEnums(List<String> values) {
		if (CollectionUtils.isEmpty(values)) {
			return List.of();
		}
		List<String> normalized = new ArrayList<>();
		for (String value : values) {
			if (!StringUtils.hasText(value)) {
				continue;
			}
			String upper = value.trim().toUpperCase(Locale.ROOT);
			if (!normalized.contains(upper)) {
				normalized.add(upper);
			}
		}
		return normalized;
	}
}
