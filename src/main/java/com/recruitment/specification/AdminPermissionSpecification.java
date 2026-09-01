package com.recruitment.specification;

import jakarta.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;

import com.recruitment.dto.request.admin.permission.AdminPermissionFilterRequest;
import com.recruitment.entity.AdminPermission;

public final class AdminPermissionSpecification {

	private AdminPermissionSpecification() {
	}

	public static Specification<AdminPermission> build(AdminPermissionFilterRequest filter) {
		return (root, query, criteriaBuilder) -> {
			Predicate predicate = criteriaBuilder.conjunction();

			String keyword = filter.normalizedKeyword();
			if (keyword != null) {
				String pattern = "%" + keyword.toLowerCase() + "%";
				Predicate nameLike = criteriaBuilder.like(
						criteriaBuilder.lower(root.get("name")), pattern);
				Predicate descriptionLike = criteriaBuilder.like(
						criteriaBuilder.lower(root.get("description")), pattern);
				Predicate pathLike = criteriaBuilder.like(
						criteriaBuilder.lower(root.get("path")), pattern);
				predicate = criteriaBuilder.and(predicate,
						criteriaBuilder.or(nameLike, descriptionLike, pathLike));
			}

			String module = filter.normalizedModule();
			if (module != null) {
				predicate = criteriaBuilder.and(predicate,
						criteriaBuilder.equal(root.get("module"), module));
			}

			String method = filter.normalizedMethod();
			if (method != null) {
				predicate = criteriaBuilder.and(predicate,
						criteriaBuilder.equal(root.get("method"), method));
			}

			if (filter.type() != null) {
				predicate = criteriaBuilder.and(predicate,
						criteriaBuilder.equal(root.get("type"), filter.type()));
			}

			if (filter.active() != null) {
				predicate = criteriaBuilder.and(predicate,
						criteriaBuilder.equal(root.get("active"), filter.active()));
			}

			return predicate;
		};
	}
}
