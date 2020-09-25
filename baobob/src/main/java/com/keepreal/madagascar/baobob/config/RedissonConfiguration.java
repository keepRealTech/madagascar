package com.keepreal.madagascar.baobob.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.api.RedissonReactiveClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Represents the redisson client.
 */
@Configuration
public class RedissonConfiguration {

    private final RedissonClient redissonClient;

    /**
     * Constructs the redisson configuration.
     *
     * @param redissonClient {@link RedissonClient}.
     */
    public RedissonConfiguration(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    /**
     * Constructs the reactive redisson client.
     *
     * @return {@link RedissonReactiveClient}.
     */
    @Bean
    public RedissonReactiveClient reactiveClient() {
        return Redisson.createReactive(redissonClient.getConfig());
    }

}
