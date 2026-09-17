package com.recruitment.util.helper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

/** Native list query cho bản ghi soft-deleted + keyword unaccent (chỉ khi có keyword). */
@Component
public class SoftDeletedPageQuery {

	private final EntityManager entityManager;

	public SoftDeletedPageQuery(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	/** Cho phép value null (khác {@code Map.of}). */
	public static Map<String, Object> params(Object... keyValues) {
		if (keyValues.length % 2 != 0) {
			throw new IllegalArgumentException("params requires even number of arguments");
		}
		Map<String, Object> map = new HashMap<>();
		for (int i = 0; i < keyValues.length; i += 2) {
			map.put((String) keyValues[i], keyValues[i + 1]);
		}
		return map;
	}

	@SuppressWarnings("unchecked")
	public <T> Page<T> find(
			Class<T> entityType,
			String fromClause,
			String alias,
			List<String> keywordColumns,
			String normalizedKeyword,
			String extraAndSql,
			Map<String, Object> params,
			String orderBy,
			Pageable pageable) {

		boolean hasKeyword = normalizedKeyword != null;
		String where = alias + ".deleted_at = 1"
				+ (hasKeyword ? " AND " + SearchHelper.nativeKeywordOrClause(keywordColumns) : "")
				+ (extraAndSql == null || extraAndSql.isBlank() ? "" : " " + extraAndSql.trim());

		String dataSql = "SELECT " + alias + ".* FROM " + fromClause
				+ " WHERE " + where
				+ " ORDER BY " + orderBy;
		String countSql = "SELECT COUNT(*) FROM " + fromClause + " WHERE " + where;

		Query dataQuery = entityManager.createNativeQuery(dataSql, entityType);
		Query countQuery = entityManager.createNativeQuery(countSql);
		if (hasKeyword) {
			String escapedKeyword = AccentHelper.escapeLike(normalizedKeyword);
			dataQuery.setParameter("keyword", escapedKeyword);
			countQuery.setParameter("keyword", escapedKeyword);
		}
		if (params != null) {
			params.forEach(dataQuery::setParameter);
			params.forEach(countQuery::setParameter);
		}

		dataQuery.setFirstResult((int) pageable.getOffset());
		dataQuery.setMaxResults(pageable.getPageSize());

		List<T> content = dataQuery.getResultList();
		long total = ((Number) countQuery.getSingleResult()).longValue();
		return new PageImpl<>(content, pageable, total);
	}
}
