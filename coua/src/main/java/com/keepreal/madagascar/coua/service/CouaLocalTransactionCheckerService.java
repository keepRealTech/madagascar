package com.keepreal.madagascar.coua.service;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.transaction.LocalTransactionChecker;
import com.aliyun.openservices.ons.api.transaction.TransactionStatus;
import com.keepreal.madagascar.coua.MergeUserAccountsEvent;
import com.keepreal.madagascar.coua.TransactionEventMessage;
import com.keepreal.madagascar.coua.TransactionEventType;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 *  Represents coua local transaction checker service.
 */
@Slf4j
@Component
public class CouaLocalTransactionCheckerService implements LocalTransactionChecker {

    private final RedissonClient redissonClient;
    private final UserInfoService userInfoService;

    public CouaLocalTransactionCheckerService(RedissonClient redissonClient,
                                              UserInfoService userInfoService) {
        this.redissonClient = redissonClient;
        this.userInfoService = userInfoService;
    }

    /**
     * check local transaction status
     *
     * @param msg {@link Message}
     * @return {@link TransactionStatus}
     */
    @Override
    public TransactionStatus check(Message msg) {

        try {

            if (Objects.isNull(msg) || Objects.isNull(msg.getBody())) {
                return TransactionStatus.RollbackTransaction;
            }

            RBucket<String> flag = this.redissonClient.getBucket(CouaLocalTransactionExecuterService.LOCAL_TRANSACTION_STATUS + msg.getKey());

            if (flag.isExists() && "yes".equals(flag.get())) {
                return TransactionStatus.CommitTransaction;
            } else {
                TransactionEventMessage transactionEventMessage = TransactionEventMessage.parseFrom(msg.getBody());
                if (transactionEventMessage.getType().equals(TransactionEventType.TRANSACTION_EVENT_MERGE_USER_ACCOUNTS)) {
                    MergeUserAccountsEvent mergeUserAccountsEvent = transactionEventMessage.getMergeUserAccountsEvent();
                    String wechatUserId = mergeUserAccountsEvent.getWechatUserId();
                    String webMobileUserId = mergeUserAccountsEvent.getWebMobileUserId();
                    this.userInfoService.mergeUserAccounts(wechatUserId, webMobileUserId);
                    return TransactionStatus.CommitTransaction;
                }
                return TransactionStatus.CommitTransaction;
            }
        } catch (Exception e) {
            return TransactionStatus.RollbackTransaction;
        }
    }

}
