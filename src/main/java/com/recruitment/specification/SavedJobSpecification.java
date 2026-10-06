package com.recruitment.specification;

import org.springframework.data.jpa.domain.Specification;

import com.recruitment.entity.SavedJob;

public final class SavedJobSpecification {

	private SavedJobSpecification() {
	}

	public static Specification<SavedJob> byUser(Long userId) {
		return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("user").get("id"), userId);
	}
}
