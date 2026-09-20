package com.recruitment.specification;

import jakarta.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;

import com.recruitment.dto.request.admin.role.AdminRoleFilterRequest;
import com.recruitment.entity.AdminRole;
import com.recruitment.util.helper.AccentHelper;
import com.recruitment.util.helper.SearchHelper;

public final class AdminRoleSpecification {

	private AdminRoleSpecification() {
	}

	public static Specification<AdminRole> build(AdminRoleFilterRequest filter) {
		return (root, query, criteriaBuilder) -> {
			Predicate predicate = criteriaBuilder.conjunction();

			String keyword = AccentHelper.normalizeForSearch(filter.keyword());
			if (keyword != null) {
				predicate = criteriaBuilder.and(predicate, criteriaBuilder.or(
						SearchHelper.likeUnaccent(criteriaBuilder, root.get("name"), keyword),
						SearchHelper.likeUnaccentNullable(criteriaBuilder, root.get("description"), keyword)));
			}

			if (filter.fullAccess() != null) {
				predicate = criteriaBuilder.and(predicate,
						criteriaBuilder.equal(root.get("fullAccess"), filter.fullAccess()));
			}

			if (filter.active() != null) {
				predicate = criteriaBuilder.and(predicate,
						criteriaBuilder.equal(root.get("active"), filter.active()));
			}

			return predicate;
		};
	}
}
