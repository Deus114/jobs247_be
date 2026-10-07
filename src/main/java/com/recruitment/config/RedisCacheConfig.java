package com.recruitment.config;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.recruitment.util.constant.CacheNames;

import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

@Configuration
@EnableCaching
public class RedisCacheConfig {

	@Bean
	RedisCacheManager cacheManager(
			RedisConnectionFactory connectionFactory,
			@Value("${app.cache.hot-jobs-ttl-seconds:120}")
			long hotJobsTtlSeconds,
			@Value("${app.cache.provinces-ttl-seconds:21600}")
			long provincesTtlSeconds,
			@Value("${app.cache.industry-groups-ttl-seconds:21600}")
			long industryGroupsTtlSeconds,
			@Value("${app.cache.education-levels-ttl-seconds:21600}")
			long educationLevelsTtlSeconds,
			@Value("${app.cache.business-config-ttl-seconds:3600}")
			long businessConfigTtlSeconds) {

		RedisCacheConfiguration defaults = baseConfig(Duration.ofMinutes(10));

		Map<String, RedisCacheConfiguration> perCache = new HashMap<>();
		perCache.put(CacheNames.HOT_JOBS, baseConfig(Duration.ofSeconds(Math.max(hotJobsTtlSeconds, 1))));
		perCache.put(CacheNames.PROVINCES, baseConfig(Duration.ofSeconds(Math.max(provincesTtlSeconds, 1))));
		perCache.put(CacheNames.INDUSTRY_GROUPS, baseConfig(Duration.ofSeconds(Math.max(industryGroupsTtlSeconds, 1))));
		perCache.put(CacheNames.EDUCATION_LEVELS,
				baseConfig(Duration.ofSeconds(Math.max(educationLevelsTtlSeconds, 1))));
		perCache.put(CacheNames.BUSINESS_CONFIG, baseConfig(Duration.ofSeconds(Math.max(businessConfigTtlSeconds, 1))));

		return RedisCacheManager.builder(connectionFactory)
				.cacheDefaults(defaults)
				.withInitialCacheConfigurations(perCache)
				.transactionAware()
				.build();
	}

	private static RedisCacheConfiguration baseConfig(Duration ttl) {
		GenericJacksonJsonRedisSerializer valueSerializer = GenericJacksonJsonRedisSerializer.builder()
				.enableDefaultTyping(BasicPolymorphicTypeValidator.builder()
						.allowIfBaseType(Object.class)
						.build())
				.build();

		return RedisCacheConfiguration.defaultCacheConfig()
				.entryTtl(ttl)
				.disableCachingNullValues()
				.serializeKeysWith(RedisSerializationContext.SerializationPair
						.fromSerializer(StringRedisSerializer.UTF_8))
				.serializeValuesWith(RedisSerializationContext.SerializationPair
						.fromSerializer(valueSerializer));
	}
}
