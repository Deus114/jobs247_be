package com.recruitment.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

@Service
public class ApiRateLimitService {

	private static final Logger log = LoggerFactory.getLogger(ApiRateLimitService.class);

	private static final String AUTH_KEY_PREFIX = "rl:auth:";
	private static final String WRITE_KEY_PREFIX = "rl:write:";
	private static final String READ_KEY_PREFIX = "rl:read:";

	private static final DefaultRedisScript<String> CONSUME_SCRIPT = new DefaultRedisScript<>();

	static {
		CONSUME_SCRIPT.setResultType(String.class);
		CONSUME_SCRIPT.setScriptText("""
				local current = redis.call('INCR', KEYS[1])
				if current == 1 then
				  redis.call('EXPIRE', KEYS[1], ARGV[1])
				end
				local ttl = redis.call('TTL', KEYS[1])
				if ttl < 1 then
				  redis.call('EXPIRE', KEYS[1], ARGV[1])
				  ttl = tonumber(ARGV[1])
				end
				return tostring(current) .. ':' .. tostring(ttl)
				""");
	}

	private final StringRedisTemplate redisTemplate;
	private final boolean enabled;
	private final int authLimit;
	private final long authWindowSeconds;
	private final int writeLimit;
	private final long writeWindowSeconds;
	private final int readLimit;
	private final long readWindowSeconds;

	public ApiRateLimitService(
			StringRedisTemplate redisTemplate,
			@Value("${app.rate-limit.enabled:true}")
			boolean enabled,
			@Value("${app.rate-limit.auth.limit:10}")
			int authLimit,
			@Value("${app.rate-limit.auth.window-seconds:60}")
			long authWindowSeconds,
			@Value("${app.rate-limit.write.limit:60}")
			int writeLimit,
			@Value("${app.rate-limit.write.window-seconds:60}")
			long writeWindowSeconds,
			@Value("${app.rate-limit.read.limit:300}")
			int readLimit,
			@Value("${app.rate-limit.read.window-seconds:60}")
			long readWindowSeconds) {
		this.redisTemplate = redisTemplate;
		this.enabled = enabled;
		this.authLimit = Math.max(authLimit, 1);
		this.authWindowSeconds = Math.max(authWindowSeconds, 1);
		this.writeLimit = Math.max(writeLimit, 1);
		this.writeWindowSeconds = Math.max(writeWindowSeconds, 1);
		this.readLimit = Math.max(readLimit, 1);
		this.readWindowSeconds = Math.max(readWindowSeconds, 1);
	}

	public boolean isEnabled() {
		return enabled;
	}

	public RateLimitDecision consumeAuth(String clientIp) {
		return consume(AUTH_KEY_PREFIX + clientIp, authLimit, authWindowSeconds, true);
	}

	public RateLimitDecision consumeWrite(String identity) {
		return consume(WRITE_KEY_PREFIX + identity, writeLimit, writeWindowSeconds, false);
	}

	public RateLimitDecision consumeRead(String identity) {
		return consume(READ_KEY_PREFIX + identity, readLimit, readWindowSeconds, false);
	}

	private RateLimitDecision consume(String key, int limit, long windowSeconds, boolean failClosed) {
		if (!enabled) {
			return RateLimitDecision.allowed();
		}
		try {
			String raw = redisTemplate.execute(
					CONSUME_SCRIPT,
					List.of(key),
					Long.toString(windowSeconds));
			return interpret(raw, limit, windowSeconds, failClosed);
		} catch (Exception ex) {
			log.warn("Rate-limit Redis failed for key={}: {}", key, ex.getMessage());
			if (failClosed) {
				return RateLimitDecision.unavailable(windowSeconds);
			}
			return RateLimitDecision.allowed();
		}
	}

	private RateLimitDecision interpret(String raw, int limit, long windowSeconds, boolean failClosed) {
		if (raw == null) {
			return failClosed
					? RateLimitDecision.unavailable(windowSeconds)
					: RateLimitDecision.allowed();
		}
		int separator = raw.indexOf(':');
		if (separator <= 0 || separator == raw.length() - 1) {
			return failClosed
					? RateLimitDecision.unavailable(windowSeconds)
					: RateLimitDecision.allowed();
		}
		long current = Long.parseLong(raw.substring(0, separator));
		long ttl = Long.parseLong(raw.substring(separator + 1));
		if (current > limit) {
			return RateLimitDecision.limited(Math.max(ttl, 1));
		}
		return RateLimitDecision.allowed();
	}
}
