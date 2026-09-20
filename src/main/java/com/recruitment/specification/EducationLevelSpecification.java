package com.recruitment.specification;

import jakarta.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;

import com.recruitment.dto.request.admin.educationlevel.EducationLevelFilterRequest;
import com.recruitment.entity.EducationLevel;
import com.recruitment.util.helper.AccentHelper;
import com.recruitment.util.helper.SearchHelper;

public final class EducationLevelSpecification {

	private EducationLevelSpecification() {
	}

	public static Specification<EducationLevel> build(EducationLevelFilterRequest filter) {
		return (root, query, criteriaBuilder) -> {
			Predicate predicate = criteriaBuilder.conjunction();

			String keyword = AccentHelper.normalizeForSearch(filter.keyword());
			if (keyword != null) {
				predicate = criteriaBuilder.and(predicate, criteriaBuilder.or(
						SearchHelper.likeUnaccent(criteriaBuilder, root.get("nameVi"), keyword),
						SearchHelper.likeUnaccent(criteriaBuilder, root.get("nameEn"), keyword)));
			}

			if (filter.active() != null) {
				predicate = criteriaBuilder.and(predicate,
						criteriaBuilder.equal(root.get("active"), filter.active()));
			}

			return predicate;
		};
	}

	public static Specification<EducationLevel> publicActive() {
		return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("active"), true);
	}
}
