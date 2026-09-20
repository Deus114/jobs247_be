package com.recruitment.specification;

import org.springframework.data.jpa.domain.Specification;

import com.recruitment.dto.request.admin.user.AdminUserFilterRequest;
import com.recruitment.entity.AdminUser;
import com.recruitment.util.helper.AccentHelper;
import com.recruitment.util.helper.SearchHelper;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;

public final class AdminUserSpecification {

	private AdminUserSpecification() {
	}

	public static Specification<AdminUser> build(AdminUserFilterRequest filter) {
		return (root, query, criteriaBuilder) -> {
			if (query != null && query.getResultType() != Long.class && query.getResultType() != long.class) {
				root.fetch("role", JoinType.LEFT);
				query.distinct(true);
			}

			Predicate predicate = criteriaBuilder.conjunction();

			String keyword = AccentHelper.normalizeForSearch(filter.keyword());
			if (keyword != null) {
				predicate = criteriaBuilder.and(predicate, criteriaBuilder.or(
						SearchHelper.likeUnaccent(criteriaBuilder, root.get("username"), keyword),
						SearchHelper.likeUnaccent(criteriaBuilder, root.get("name"), keyword)));
			}

			if (filter.roleId() != null) {
				predicate = criteriaBuilder.and(predicate,
						criteriaBuilder.equal(root.get("role").get("id"), filter.roleId()));
			}

			if (filter.active() != null) {
				predicate = criteriaBuilder.and(predicate,
						criteriaBuilder.equal(root.get("active"), filter.active()));
			}

			return predicate;
		};
	}
}
