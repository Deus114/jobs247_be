package com.recruitment.specification;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import com.recruitment.dto.request.admin.job.AdminJobFilterRequest;
import com.recruitment.dto.request.user.job.EmployerJobFilterRequest;
import com.recruitment.entity.Job;
import com.recruitment.util.helper.AccentHelper;
import com.recruitment.util.helper.SearchHelper;

public final class JobSpecification {

	private JobSpecification() {
	}

	public static Specification<Job> buildEmployer(
			java.util.Collection<Long> companyIds,
			EmployerJobFilterRequest filter) {
		return (root, query, criteriaBuilder) -> {
			if (query != null) {
				query.distinct(true);
			}
			Predicate predicate = root.get("company").get("id").in(companyIds);
			predicate = applyCommon(
					predicate,
					root,
					query,
					criteriaBuilder,
					filter.keyword(),
					filter.status(),
					filter.companyId(),
					filter.industryId(),
					filter.provinceId(),
					filter.employmentType(),
					filter.experienceLevel(),
					filter.active(),
					filter.hot(),
					filter.fromDate(),
					filter.toDate());
			return predicate;
		};
	}

	public static Specification<Job> buildAdmin(AdminJobFilterRequest filter) {
		return (root, query, criteriaBuilder) -> {
			if (query != null) {
				query.distinct(true);
			}
			return applyCommon(
					criteriaBuilder.conjunction(),
					root,
					query,
					criteriaBuilder,
					filter.keyword(),
					filter.status(),
					filter.companyId(),
					filter.industryId(),
					filter.provinceId(),
					filter.employmentType(),
					filter.experienceLevel(),
					filter.active(),
					filter.hot(),
					filter.fromDate(),
					filter.toDate());
		};
	}

	private static Predicate applyCommon(
			Predicate predicate,
			jakarta.persistence.criteria.Root<Job> root,
			jakarta.persistence.criteria.CriteriaQuery<?> query,
			jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder,
			String keyword,
			String status,
			Long companyId,
			Long industryId,
			Long provinceId,
			String employmentType,
			String experienceLevel,
			Boolean active,
			Boolean hot,
			LocalDate fromDate,
			LocalDate toDate) {

		String normalizedKeyword = AccentHelper.normalizeForSearch(keyword);
		if (normalizedKeyword != null) {
			predicate = criteriaBuilder.and(predicate, criteriaBuilder.or(
					SearchHelper.likeUnaccent(criteriaBuilder, root.get("title"), normalizedKeyword),
					SearchHelper.likeUnaccent(criteriaBuilder, root.get("company").get("name"), normalizedKeyword)));
		}

		if (status != null && !status.isBlank()) {
			predicate = criteriaBuilder.and(predicate,
					criteriaBuilder.equal(root.get("status"), status.trim().toUpperCase()));
		}
		if (companyId != null) {
			predicate = criteriaBuilder.and(predicate,
					criteriaBuilder.equal(root.get("company").get("id"), companyId));
		}
		if (industryId != null) {
			Join<?, ?> industries = root.join("industries");
			predicate = criteriaBuilder.and(predicate,
					criteriaBuilder.equal(industries.get("id"), industryId));
		}
		if (provinceId != null) {
			Join<?, ?> provinces = root.join("provinces");
			predicate = criteriaBuilder.and(predicate,
					criteriaBuilder.equal(provinces.get("id"), provinceId));
		}
		if (StringUtils.hasText(employmentType)) {
			Join<?, ?> types = root.join("employmentTypes");
			predicate = criteriaBuilder.and(predicate,
					criteriaBuilder.equal(types, employmentType.trim().toUpperCase()));
		}
		if (StringUtils.hasText(experienceLevel)) {
			Join<?, ?> levels = root.join("experienceLevels");
			predicate = criteriaBuilder.and(predicate,
					criteriaBuilder.equal(levels, experienceLevel.trim().toUpperCase()));
		}
		if (active != null) {
			predicate = criteriaBuilder.and(predicate,
					criteriaBuilder.equal(root.get("active"), active));
		}
		if (hot != null) {
			predicate = criteriaBuilder.and(predicate,
					criteriaBuilder.equal(root.get("hot"), hot));
		}
		if (fromDate != null) {
			Instant from = fromDate.atStartOfDay().toInstant(ZoneOffset.UTC);
			predicate = criteriaBuilder.and(predicate,
					criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), from));
		}
		if (toDate != null) {
			Instant to = toDate.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
			predicate = criteriaBuilder.and(predicate,
					criteriaBuilder.lessThan(root.get("createdAt"), to));
		}
		return predicate;
	}
}
