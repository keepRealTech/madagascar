package com.keepreal.madagascar.lemur.config;

import org.ehcache.jsr107.EhcacheCachingProvider;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.spring.cache.CacheConfig;
import org.redisson.spring.cache.RedissonSpringCacheManager;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.jcache.JCacheCacheManager;
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

    /**
     * Constructs the caching configurations.
     *
     * @param redissonClient {@link RedissonClient}.
     */
    public CacheConfiguration(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    /**
     * Represents the default in memory concurrent map for caching.
     * @Note Use only for ResponseEntity controller level caching. Do not abuse it.
     *
     * @return {@link ConcurrentMapCacheManager}.
     */
    @Bean
    @Primary
    public CacheManager defaultCacheManager() {
        return new ConcurrentMapCacheManager();
    }

    /**
     * Represents the local ehcache for caching. This can be delicately configured and use local disk.
     *
     * @return {@link JCacheCacheManager}.
     * @throws URISyntaxException Uri exception.
     */
    @Bean
    public CacheManager ehcacheCacheManager() throws URISyntaxException {
        CachingProvider provider = Caching.getCachingProvider(EhcacheCachingProvider.class.getName());
        javax.cache.CacheManager cacheManager = provider.getCacheManager(
                Objects.requireNonNull(this.getClass().getClassLoader().getResource("ehcache-3.xml")).toURI(),
                this.getClass().getClassLoader()
        );
        return new JCacheCacheManager(cacheManager);
    }

    /**
     * Represents the redis for caching.
     *
     * @return {@link RedissonSpringCacheManager}.
     */
    @Bean
    public CacheManager redisCacheManager() {
        Map<String, CacheConfig> config = new HashMap<>(16);
        config.put("IslandMessage", new CacheConfig(0, 24 * 60 * 60 * 1000));
        return new RedissonSpringCacheManager(this.redissonClient, config);
    }

}
