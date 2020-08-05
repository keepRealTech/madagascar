package com.keepreal.madagascar.lemur.config;

import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.jsr107.EhcacheCachingProvider;
import org.ehcache.xml.XmlConfiguration;
import org.redisson.api.RedissonClient;
import org.redisson.spring.cache.CacheConfig;
import org.redisson.spring.cache.RedissonSpringCacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.cache.Caching;
import javax.cache.spi.CachingProvider;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents the caching configurations.
 */
@EnableCaching
@Configuration
public class CacheConfiguration {

    private final RedissonClient redissonClient;

    public CacheConfiguration(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Bean
    @Primary
    public CacheManager ehCacheCacheManager() throws URISyntaxException {
        CachingProvider provider = Caching.getCachingProvider(EhcacheCachingProvider.class.getName());
        javax.cache.CacheManager cacheManager = provider.getCacheManager(
                Objects.requireNonNull(this.getClass().getClassLoader().getResource("ehcache-3.xml")).toURI(),
                this.getClass().getClassLoader()
        );
        return new JCacheCacheManager(cacheManager);
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
