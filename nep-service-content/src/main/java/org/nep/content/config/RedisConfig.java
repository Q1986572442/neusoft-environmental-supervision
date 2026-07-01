package org.nep.content.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis 缓存配置
 * 热点数据缓存策略：新闻首页30min / 知识库1h / 统计10min
 *
 * @author NEP Team
 */
@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        Jackson2JsonRedisSerializer<Object> jacksonSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setValueSerializer(jacksonSerializer);
        template.setHashValueSerializer(jacksonSerializer);
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new Jackson2JsonRedisSerializer<>(Object.class)))
                .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
        // 新闻缓存 30分钟
        cacheConfigs.put("news", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        // 知识库缓存 1小时
        cacheConfigs.put("knowledge", defaultConfig.entryTtl(Duration.ofHours(1)));
        // 统计缓存 10分钟
        cacheConfigs.put("statistics", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        // AQI 地图缓存 10分钟（热点数据，与DB聚合的时效性匹配）
        cacheConfigs.put("aqi-map", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        // AQI 排行缓存 5分钟
        cacheConfigs.put("aqi", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        // 布隆过滤器元数据缓存 1小时
        cacheConfigs.put("bloom-meta", defaultConfig.entryTtl(Duration.ofHours(1)));

        return RedisCacheManager.builder(factory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }
}
