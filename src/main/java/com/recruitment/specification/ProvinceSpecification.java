package com.recruitment.specification;

import jakarta.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;

import com.recruitment.dto.request.admin.province.ProvinceFilterRequest;
import com.recruitment.entity.Province;
import com.recruitment.util.helper.SearchHelper;

public final class ProvinceSpecification {

	private ProvinceSpecification() {
	}

	public static Specification<Province> build(ProvinceFilterRequest filter) {
		return (root, query, criteriaBuilder) -> {
			Predicate predicate = criteriaBuilder.conjunction();

			String keyword = filter.normalizedKeyword();
			if (keyword != null) {
				predicate = criteriaBuilder.and(predicate,
						SearchHelper.likeUnaccent(criteriaBuilder, root.get("name"), keyword));
			}

			String region = filter.normalizedRegion();
			if (region != null) {
				predicate = criteriaBuilder.and(predicate,
						criteriaBuilder.equal(root.get("region"), region));
			}

			if (filter.active() != null) {
				predicate = criteriaBuilder.and(predicate,
						criteriaBuilder.equal(root.get("active"), filter.active()));
			}

			return predicate;
		};
	}

	public static Specification<Province> publicActive() {
		return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("active"), true);
	}
}
