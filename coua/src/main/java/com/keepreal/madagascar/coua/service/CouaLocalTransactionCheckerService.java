package com.keepreal.madagascar.coua.service;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.transaction.LocalTransactionChecker;
import com.aliyun.openservices.ons.api.transaction.TransactionStatus;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

/**
 *  Represents coua local transaction checker service.
 */
@Slf4j
@Component
public class CouaLocalTransactionCheckerService implements LocalTransactionChecker {

    private final RedissonClient redissonClient;

    public CouaLocalTransactionCheckerService(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    /**
     * check local transaction status
     *
     * @param msg {@link Message}
     * @return {@link TransactionStatus}
     */
    @Override
    public TransactionStatus check(Message msg) {
        RBucket<String> flag = this.redissonClient.getBucket(CouaLocalTransactionExecuterService.LOCAL_TRANSACTION_STATUS + msg.getKey());
        if (flag.isExists() && "yes".equals(flag.get())) {
            return TransactionStatus.CommitTransaction;
        }
        return TransactionStatus.RollbackTransaction;
    }
}
