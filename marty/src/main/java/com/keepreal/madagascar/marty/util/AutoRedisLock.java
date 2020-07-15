package com.keepreal.madagascar.marty.util;

import lombok.Data;
import lombok.SneakyThrows;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

/**
 * Represents a redis lock with auto closable interface.
 */
@Data
public class AutoRedisLock implements AutoCloseable {

    private RLock lock;

    /**
     * Constructs a lock.
     *
     * @param client   {@link RedissonClient}.
     * @param lockName Lock name.
     */
    @SneakyThrows
    public AutoRedisLock(RedissonClient client, String lockName) {
        this(client, lockName, 5, 1000);
    }

    /**
     * Constructs a lock.
     *
     * @param client        {@link RedissonClient}.
     * @param lockName      Lock name.
     * @param waitTimeInMS  Wait time in milliseconds.
     * @param leaseTimeInMS Lease time in milliseconds.
     */
    @SneakyThrows
    public AutoRedisLock(RedissonClient client, String lockName, long waitTimeInMS, long leaseTimeInMS) {
        this.lock = client.getLock(lockName);
        assert this.lock.tryLock(waitTimeInMS, leaseTimeInMS, TimeUnit.MILLISECONDS);
    }

    /**
     * Implements the close logic.
     */
    @Override
    public void close() {
        this.lock.forceUnlock();
    }

}
