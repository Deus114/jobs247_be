package com.recruitment.specification;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Collection;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import com.recruitment.dto.request.admin.application.AdminApplicationFilterRequest;
import com.recruitment.dto.request.user.application.EmployerApplicationFilterRequest;
import com.recruitment.dto.request.user.application.JobSeekerApplicationFilterRequest;
import com.recruitment.entity.JobApplication;
import com.recruitment.util.helper.AccentHelper;
import com.recruitment.util.helper.SearchHelper;

import jakarta.persistence.criteria.Predicate;

public final class JobApplicationSpecification {

	private JobApplicationSpecification() {
	}

	public static Specification<JobApplication> buildJobSeeker(Long userId, JobSeekerApplicationFilterRequest filter) {
		return (root, query, criteriaBuilder) -> {
			Predicate predicate = criteriaBuilder.equal(root.get("user").get("id"), userId);
			return applyCommon(
					predicate,
					root,
					criteriaBuilder,
					filter.keyword(),
					filter.status(),
					null,
					filter.jobId(),
					filter.fromDate(),
					filter.toDate());
		};
	}

	public static Specification<JobApplication> buildEmployer(
			Collection<Long> companyIds,
			EmployerApplicationFilterRequest filter) {
		return (root, query, criteriaBuilder) -> {
			Predicate predicate = root.get("job").get("company").get("id").in(companyIds);
			return applyCommon(
					predicate,
					root,
					criteriaBuilder,
					filter.keyword(),
					filter.status(),
					filter.companyId(),
					filter.jobId(),
					filter.fromDate(),
					filter.toDate());
		};
	}

	public static Specification<JobApplication> buildAdmin(AdminApplicationFilterRequest filter) {
		return (root, query, criteriaBuilder) -> applyCommon(
				criteriaBuilder.conjunction(),
				root,
				criteriaBuilder,
				filter.keyword(),
				filter.status(),
				filter.companyId(),
				filter.jobId(),
				filter.fromDate(),
				filter.toDate());
	}

	private static Predicate applyCommon(
			Predicate predicate,
			jakarta.persistence.criteria.Root<JobApplication> root,
			jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder,
			String keyword,
			String status,
			Long companyId,
			Long jobId,
			LocalDate fromDate,
			LocalDate toDate) {

		String normalizedKeyword = AccentHelper.normalizeForSearch(keyword);
		if (normalizedKeyword != null) {
			predicate = criteriaBuilder.and(predicate, criteriaBuilder.or(
					SearchHelper.likeUnaccent(criteriaBuilder, root.get("fullName"), normalizedKeyword),
					SearchHelper.likeUnaccent(criteriaBuilder, root.get("email"), normalizedKeyword),
					SearchHelper.likeUnaccent(criteriaBuilder, root.get("job").get("title"), normalizedKeyword)));
		}
		if (StringUtils.hasText(status)) {
			predicate = criteriaBuilder.and(predicate,
					criteriaBuilder.equal(root.get("status"), status.trim().toUpperCase()));
		}
		if (companyId != null) {
			predicate = criteriaBuilder.and(predicate,
					criteriaBuilder.equal(root.get("job").get("company").get("id"), companyId));
		}
		if (jobId != null) {
			predicate = criteriaBuilder.and(predicate,
					criteriaBuilder.equal(root.get("job").get("id"), jobId));
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
