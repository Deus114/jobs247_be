package com.recruitment.specification;

import org.springframework.data.jpa.domain.Specification;

import com.recruitment.dto.request.user.company.EmployerCompanyFilterRequest;
import com.recruitment.entity.Company;
import com.recruitment.entity.CompanyMember;
import com.recruitment.util.helper.AccentHelper;
import com.recruitment.util.helper.SearchHelper;

import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

public final class CompanySpecification {

	private CompanySpecification() {
	}

	public static Specification<Company> build(EmployerCompanyFilterRequest filter, Long employerUserId) {
		return (root, query, criteriaBuilder) -> {
			Predicate predicate = criteriaBuilder.conjunction();

			String keyword = AccentHelper.normalizeForSearch(filter.keyword());
			if (keyword != null) {
				predicate = criteriaBuilder.and(predicate, criteriaBuilder.or(
						SearchHelper.likeUnaccent(criteriaBuilder, root.get("name"), keyword),
						SearchHelper.likeUnaccent(criteriaBuilder, root.get("email"), keyword),
						SearchHelper.likeUnaccent(criteriaBuilder, root.get("taxCode"), keyword)));
			}

			if (filter.status() != null && !filter.status().isBlank()
					&& !Boolean.FALSE.equals(filter.mine())) {
				predicate = criteriaBuilder.and(predicate,
						criteriaBuilder.equal(root.get("status"), filter.status().trim().toUpperCase()));
			}

			if (filter.active() != null) {
				predicate = criteriaBuilder.and(predicate,
						criteriaBuilder.equal(root.get("active"), filter.active()));
			}

			if (filter.mine() != null && employerUserId != null) {
				Subquery<Long> memberCompanyIds = query.subquery(Long.class);
				Root<CompanyMember> memberRoot = memberCompanyIds.from(CompanyMember.class);
				memberCompanyIds.select(memberRoot.get("company").get("id"))
						.where(criteriaBuilder.equal(memberRoot.get("user").get("id"), employerUserId));

				if (Boolean.TRUE.equals(filter.mine())) {
					predicate = criteriaBuilder.and(predicate, root.get("id").in(memberCompanyIds));
				} else {
					predicate = criteriaBuilder.and(predicate,
							criteriaBuilder.not(root.get("id").in(memberCompanyIds)),
							criteriaBuilder.equal(root.get("status"), "APPROVED"));
				}
			}

			return predicate;
		};
	}
}
