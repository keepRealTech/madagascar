package com.keepreal.madagascar.lemur.config.grpc;

import org.redisson.api.RedissonClient;
import org.redisson.spring.cache.CacheConfig;
import org.redisson.spring.cache.RedissonSpringCacheManager;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the caching configurations.
 */
@EnableCaching
@Configuration
public class CacheConfiguration {

    private final JCacheCacheManager echcacheManager;
    private final RedissonClient redissonClient;

    public CacheConfiguration(JCacheCacheManager echcacheManager,
                              RedissonClient redissonClient) {
        this.echcacheManager = echcacheManager;
        this.redissonClient = redissonClient;
    }

    @Bean
    @Primary
    public CacheManager cacheManager() {
        return this.echcacheManager;
    }

    @Bean
    public CacheManager redisCacheManager() {
        Map<String, CacheConfig> config = new HashMap<>(16);
        // create "testMap" cache with ttl = 24 minutes and maxIdleTime = 12 minutes
        config.put("user", new CacheConfig(20*1000, 12 * 60 * 1000));
        config.put("people", new CacheConfig(30*1000, 12 * 60 * 1000));
        return new RedissonSpringCacheManager(this.redissonClient, config);
    }

}
