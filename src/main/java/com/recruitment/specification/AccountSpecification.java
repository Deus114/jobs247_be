package com.recruitment.specification;

import jakarta.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;

import com.recruitment.dto.request.admin.account.AccountFilterRequest;
import com.recruitment.entity.User;
import com.recruitment.util.helper.AccentHelper;
import com.recruitment.util.helper.SearchHelper;

public final class AccountSpecification {

	private AccountSpecification() {
	}

	public static Specification<User> build(AccountFilterRequest filter) {
		return (root, query, criteriaBuilder) -> {
			Predicate predicate = criteriaBuilder.conjunction();

			String keyword = AccentHelper.normalizeForSearch(filter.keyword());
			if (keyword != null) {
				predicate = criteriaBuilder.and(predicate, criteriaBuilder.or(
						SearchHelper.likeUnaccent(criteriaBuilder, root.get("name"), keyword),
						SearchHelper.likeUnaccent(criteriaBuilder, root.get("email"), keyword)));
			}

			if (filter.type() != null) {
				predicate = criteriaBuilder.and(predicate,
						criteriaBuilder.equal(root.get("type"), filter.type().name()));
			}

			if (filter.active() != null) {
				predicate = criteriaBuilder.and(predicate,
						criteriaBuilder.equal(root.get("active"), filter.active()));
			}

			return predicate;
		};
	}
}
