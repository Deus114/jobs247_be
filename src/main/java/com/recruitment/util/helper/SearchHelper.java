package com.recruitment.util.helper;

import java.util.List;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;

/** So khớp keyword đã normalize với cột DB (unaccent phía SQL). */
public final class SearchHelper {

	private SearchHelper() {
	}

	public static Predicate likeUnaccent(
			CriteriaBuilder cb,
			Expression<String> expression,
			String normalizedKeyword) {
		return cb.like(
				unaccentLower(cb, expression),
				"%" + AccentHelper.escapeLike(normalizedKeyword) + "%",
				'\\');
	}

	public static Predicate likeUnaccentNullable(
			CriteriaBuilder cb,
			Expression<String> expression,
			String normalizedKeyword) {
		return likeUnaccent(cb, cb.coalesce(expression, ""), normalizedKeyword);
	}

	public static String nativeKeywordOrClause(List<String> columns) {
		StringBuilder sql = new StringBuilder("(");
		for (int i = 0; i < columns.size(); i++) {
			if (i > 0) {
				sql.append(" OR ");
			}
			sql.append(unaccentSql(columns.get(i)))
					.append(" LIKE CONCAT('%', :keyword, '%') ESCAPE '\\\\'");
		}
		sql.append(')');
		return sql.toString();
	}

	private static Expression<String> unaccentLower(CriteriaBuilder cb, Expression<String> expression) {
		Expression<String> expr = cb.lower(expression);
		for (String[] pair : AccentHelper.LOWERCASE_REPLACEMENTS) {
			expr = cb.function("replace", String.class, expr, cb.literal(pair[0]), cb.literal(pair[1]));
		}
		return expr;
	}

	private static String unaccentSql(String columnExpression) {
		String expr = "LOWER(" + columnExpression + ")";
		for (String[] pair : AccentHelper.LOWERCASE_REPLACEMENTS) {
			expr = "REPLACE(" + expr + ", '" + pair[0] + "', '" + pair[1] + "')";
		}
		return expr;
	}
}
