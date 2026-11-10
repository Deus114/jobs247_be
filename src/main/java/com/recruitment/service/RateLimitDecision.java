package com.recruitment.service;

public record RateLimitDecision(Outcome outcome, long retryAfterSeconds) {

	public enum Outcome {
		ALLOWED,
		LIMITED,
		UNAVAILABLE
	}

	public static RateLimitDecision allowed() {
		return new RateLimitDecision(Outcome.ALLOWED, 0);
	}

	public static RateLimitDecision limited(long retryAfterSeconds) {
		return new RateLimitDecision(Outcome.LIMITED, retryAfterSeconds);
	}

	public static RateLimitDecision unavailable(long retryAfterSeconds) {
		return new RateLimitDecision(Outcome.UNAVAILABLE, retryAfterSeconds);
	}
}
