package com.recruitment.specification;

import jakarta.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;

import com.recruitment.dto.request.admin.company.AdminCompanyFilterRequest;
import com.recruitment.entity.Company;
import com.recruitment.util.helper.AccentHelper;
import com.recruitment.util.helper.SearchHelper;

public final class AdminCompanySpecification {

	private AdminCompanySpecification() {
	}

	public static Specification<Company> build(AdminCompanyFilterRequest filter) {
		return (root, query, criteriaBuilder) -> {
			Predicate predicate = criteriaBuilder.conjunction();

			String keyword = AccentHelper.normalizeForSearch(filter.keyword());
			if (keyword != null) {
				predicate = criteriaBuilder.and(predicate, criteriaBuilder.or(
						SearchHelper.likeUnaccent(criteriaBuilder, root.get("name"), keyword),
						SearchHelper.likeUnaccent(criteriaBuilder, root.get("email"), keyword),
						SearchHelper.likeUnaccent(criteriaBuilder, root.get("taxCode"), keyword)));
			}

			if (filter.status() != null && !filter.status().isBlank()) {
				predicate = criteriaBuilder.and(predicate,
						criteriaBuilder.equal(root.get("status"), filter.status().trim().toUpperCase()));
			}

			if (filter.active() != null) {
				predicate = criteriaBuilder.and(predicate,
						criteriaBuilder.equal(root.get("active"), filter.active()));
			}

			return predicate;
		};
	}
}
