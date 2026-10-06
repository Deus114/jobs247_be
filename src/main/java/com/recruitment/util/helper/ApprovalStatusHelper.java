package com.recruitment.util.helper;

import java.time.Instant;
import java.util.Locale;

import org.springframework.util.StringUtils;

import com.recruitment.exception.BusinessRuleException;

public final class ApprovalStatusHelper {

	private ApprovalStatusHelper() {
	}

	public record ApprovalFields(
			String status,
			String rejectionReason,
			Instant rejectedAt,
			Instant approvedAt) {
	}

	public static ApprovalFields apply(String rawStatus, String rejectionReason, String invalidStatusMessageKey) {
		if (!StringUtils.hasText(rawStatus)) {
			throw new BusinessRuleException(invalidStatusMessageKey, rawStatus);
		}
		String status = rawStatus.trim().toUpperCase(Locale.ROOT);
		return switch (status) {
		case "REJECTED" -> new ApprovalFields(
				status,
				trimToNull(rejectionReason),
				Instant.now(),
				null);
		case "APPROVED" -> new ApprovalFields(
				status,
				null,
				null,
				Instant.now());
		case "PENDING" -> new ApprovalFields(
				status,
				null,
				null,
				null);
		default -> throw new BusinessRuleException(invalidStatusMessageKey, rawStatus);
		};
	}

	private static String trimToNull(String value) {
		if (!StringUtils.hasText(value)) {
			return null;
		}
		return value.trim();
	}
}
