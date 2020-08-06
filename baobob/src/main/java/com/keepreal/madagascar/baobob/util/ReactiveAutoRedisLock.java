package com.keepreal.madagascar.baobob.util;

import lombok.Data;
import lombok.SneakyThrows;
import org.redisson.api.RLock;
import org.redisson.api.RLockReactive;
import org.redisson.api.RedissonClient;
import org.redisson.api.RedissonReactiveClient;

import java.util.ConcurrentModificationException;
import java.util.concurrent.TimeUnit;

/**
 * Represents a redis lock with auto closable interface.
 */
@Data
public class ReactiveAutoRedisLock implements AutoCloseable {

    private RLockReactive lock;

    /**
     * Constructs a lock.
     *
     * @param client   {@link RedissonReactiveClient}.
     * @param lockName Lock name.
     */
    public ReactiveAutoRedisLock(RedissonReactiveClient client, String lockName) {
        this(client, lockName, 500, 500);
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
    public ReactiveAutoRedisLock(RedissonReactiveClient client, String lockName, long waitTimeInMS, long leaseTimeInMS) {
        this.lock = client.getLock(lockName);
        if (!Boolean.TRUE.equals(this.lock.tryLock(waitTimeInMS, leaseTimeInMS, TimeUnit.MILLISECONDS).block())) {
            throw new ConcurrentModificationException("Failed to acquire redis lock: " + lockName);
        }
    }

    /**
     * Implements the close logic.
     */
    @Override
    public void close() {
        this.lock.forceUnlock();
    }

}
