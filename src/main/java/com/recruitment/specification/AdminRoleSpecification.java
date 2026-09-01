package com.recruitment.specification;

import jakarta.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;

import com.recruitment.dto.request.admin.role.AdminRoleFilterRequest;
import com.recruitment.entity.AdminRole;

public final class AdminRoleSpecification {

	private AdminRoleSpecification() {
	}

	public static Specification<AdminRole> build(AdminRoleFilterRequest filter) {
		return (root, query, criteriaBuilder) -> {
			Predicate predicate = criteriaBuilder.conjunction();

			String keyword = filter.normalizedKeyword();
			if (keyword != null) {
				String pattern = "%" + keyword.toLowerCase() + "%";
				Predicate nameLike = criteriaBuilder.like(
						criteriaBuilder.lower(root.get("name")), pattern);
				Predicate descriptionLike = criteriaBuilder.like(
						criteriaBuilder.lower(root.get("description")), pattern);
				predicate = criteriaBuilder.and(predicate, criteriaBuilder.or(nameLike, descriptionLike));
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
