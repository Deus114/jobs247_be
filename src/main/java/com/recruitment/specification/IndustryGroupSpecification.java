package com.recruitment.specification;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;

import com.recruitment.dto.request.admin.industrygroup.IndustryGroupFilterRequest;
import com.recruitment.entity.IndustryGroup;
import com.recruitment.util.helper.AccentHelper;
import com.recruitment.util.helper.SearchHelper;

public final class IndustryGroupSpecification {

	private IndustryGroupSpecification() {
	}

	public static Specification<IndustryGroup> build(IndustryGroupFilterRequest filter) {
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

			return predicate;
		};
	}

	/** Sort theo số ngành. Gọi kèm Pageable không có Sort để tránh trùng ORDER BY. */
	public static Specification<IndustryGroup> orderByIndustryCount(boolean ascending) {
		return (root, query, criteriaBuilder) -> {
			if (query.getResultType() != Long.class && query.getResultType() != long.class) {
				Expression<Integer> industryCount = criteriaBuilder.size(root.get("industries"));
				query.orderBy(
						ascending ? criteriaBuilder.asc(industryCount) : criteriaBuilder.desc(industryCount),
						criteriaBuilder.asc(root.get("id")));
			}
			return null;
		};
	}

	public static Specification<IndustryGroup> publicActive() {
		return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("active"), true);
	}
}
