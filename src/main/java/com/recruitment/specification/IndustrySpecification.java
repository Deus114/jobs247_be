package com.recruitment.specification;

import jakarta.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;

import com.recruitment.dto.request.admin.industry.IndustryFilterRequest;
import com.recruitment.entity.Industry;
import com.recruitment.util.helper.AccentHelper;
import com.recruitment.util.helper.SearchHelper;

public final class IndustrySpecification {

	private IndustrySpecification() {
	}

	public static Specification<Industry> build(IndustryFilterRequest filter) {
		return (root, query, criteriaBuilder) -> {
			Predicate predicate = criteriaBuilder.conjunction();

			String keyword = AccentHelper.normalizeForSearch(filter.keyword());
			if (keyword != null) {
				predicate = criteriaBuilder.and(predicate, criteriaBuilder.or(
						SearchHelper.likeUnaccent(criteriaBuilder, root.get("nameVi"), keyword),
						SearchHelper.likeUnaccent(criteriaBuilder, root.get("nameEn"), keyword),
						SearchHelper.likeUnaccentNullable(criteriaBuilder, root.get("descriptionVi"), keyword),
						SearchHelper.likeUnaccentNullable(criteriaBuilder, root.get("descriptionEn"), keyword)));
			}

			if (filter.active() != null) {
				predicate = criteriaBuilder.and(predicate,
						criteriaBuilder.equal(root.get("active"), filter.active()));
			}

			if (filter.industryGroupId() != null) {
				predicate = criteriaBuilder.and(predicate,
						criteriaBuilder.equal(root.get("industryGroup").get("id"), filter.industryGroupId()));
			}

			return predicate;
		};
	}
}
