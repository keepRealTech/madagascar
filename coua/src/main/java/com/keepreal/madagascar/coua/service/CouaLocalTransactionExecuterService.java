package com.keepreal.madagascar.coua.service;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.transaction.LocalTransactionExecuter;
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
 *  Represents coua local transaction executer service.
 */
@Slf4j
@Component
public class CouaLocalTransactionExecuterService implements LocalTransactionExecuter {

    public final static String LOCAL_TRANSACTION_STATUS = "localTrans_";
    private final UserInfoService userInfoService;
    private final RedissonClient redissonClient;

    public CouaLocalTransactionExecuterService(UserInfoService userInfoService,
                                               RedissonClient redissonClient) {
        this.userInfoService = userInfoService;
        this.redissonClient = redissonClient;
    }

    /**
     * execute local transaction
     *
     * @param msg   {@link Message}
     * @param arg    external arg
     * @return      {@link Message}
     */
    @Override
    public TransactionStatus execute(Message msg, Object arg) {

        try {
            if (Objects.isNull(msg) || Objects.isNull(msg.getBody())) {
                return TransactionStatus.RollbackTransaction;
            }
            TransactionEventMessage transactionEventMessage = TransactionEventMessage.parseFrom(msg.getBody());

            if (transactionEventMessage.getType().equals(TransactionEventType.TRANSACTION_EVENT_MERGE_USER_ACCOUNTS)) {
                MergeUserAccountsEvent mergeUserAccountsEvent = transactionEventMessage.getMergeUserAccountsEvent();
                String webMobileUserId = mergeUserAccountsEvent.getWebMobileUserId();
                String wechatUserId = mergeUserAccountsEvent.getWechatUserId();
                this.userInfoService.mergeUserAccounts(wechatUserId, webMobileUserId);
                RBucket<String> flag = this.redissonClient.getBucket(LOCAL_TRANSACTION_STATUS + msg.getKey());
                flag.set("yes", 10L, TimeUnit.MINUTES);
                return TransactionStatus.CommitTransaction;
            }
        } catch (Exception e) {
            return TransactionStatus.RollbackTransaction;
        }
        return TransactionStatus.RollbackTransaction;
    }
}
