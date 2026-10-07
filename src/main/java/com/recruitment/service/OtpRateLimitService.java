package com.recruitment.service;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class OtpRateLimitService {

	private static final Logger log = LoggerFactory.getLogger(OtpRateLimitService.class);

	private final StringRedisTemplate redisTemplate;

	public OtpRateLimitService(StringRedisTemplate redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	/**
	 * @return {@code true} nếu được phép gửi OTP; {@code false} nếu còn trong cooldown.
	 * Redis lỗi → cho phép (fallback DB cooldown trong UserAuthService).
	 */
	public boolean tryAcquire(String purpose, String email, Duration cooldown) {
		String key = "otp:cooldown:" + purpose + ":" + email;
		try {
			Boolean acquired = redisTemplate.opsForValue().setIfAbsent(key, "1", cooldown);
			return !Boolean.FALSE.equals(acquired);
		} catch (Exception ex) {
			log.warn("OTP rate-limit Redis failed for key={}: {}", key, ex.getMessage());
			return true;
		}
	}
}
