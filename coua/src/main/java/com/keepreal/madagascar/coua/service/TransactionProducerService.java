package com.keepreal.madagascar.coua.service;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.SendResult;
import com.aliyun.openservices.ons.api.bean.TransactionProducerBean;
import com.keepreal.madagascar.coua.MergeUserAccountsEvent;
import com.keepreal.madagascar.coua.TransactionEventMessage;
import com.keepreal.madagascar.coua.TransactionEventType;
import com.keepreal.madagascar.coua.config.TransactionProducerConfiguration;
import com.keepreal.madagascar.coua.model.MergeUserLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Represents the transaction message producer.
 */
@Service
@Slf4j
public class TransactionProducerService {

    private final TransactionProducerBean transactionProducerBean;
    private final TransactionProducerConfiguration mergerTransactionConfiguration;
    private final ExecutorService executorService;
    private final CouaLocalTransactionExecuterService couaLocalTransactionExecuterService;
    private final MergeUserLogService mergeUserLogService;

    /**
     * Constructs the transaction event producer service.
     *
     * @param producerBean              Notification event producer bean.
     * @param mergerTransactionConfiguration       {@link TransactionProducerConfiguration}.
     */
    public TransactionProducerService(@Qualifier("transaction-event-producer") TransactionProducerBean producerBean,
                                      TransactionProducerConfiguration mergerTransactionConfiguration,
                                      CouaLocalTransactionExecuterService couaLocalTransactionExecuterService,
                                      MergeUserLogService mergeUserLogService) {
        this.transactionProducerBean = producerBean;
        this.mergerTransactionConfiguration = mergerTransactionConfiguration;
        this.couaLocalTransactionExecuterService = couaLocalTransactionExecuterService;
        this.mergeUserLogService = mergeUserLogService;
        this.executorService = new ThreadPoolExecutor(
                10,
                20,
                1L,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(500000),
                new CustomizableThreadFactory("transaction-event-producer-"));
    }


    public void produceMergeUserAccountsTransactionEventAsync(String wechatUserId, String webMobileUserId) {
        Message message = this.createMergeUserAccountsTransactionEvent(wechatUserId, webMobileUserId);
        this.sendTransactionAsync(message);
        MergeUserLog mergeUserLog = MergeUserLog.builder()
                .wechatUid(wechatUserId)
                .mobileUid(webMobileUserId)
                .eventId(message.getKey())
                .build();
        this.mergeUserLogService.createNewMergeUserLog(mergeUserLog);
    }

    /**
     * 创建合并用户的事务消息
     * @param wechatUserId      wechat user id
     * @param webMobileUserId   web user id
     * @return {@link Message}
     */
    private Message createMergeUserAccountsTransactionEvent(String wechatUserId, String webMobileUserId) {
        MergeUserAccountsEvent mergeUserAccountsEvent = MergeUserAccountsEvent.newBuilder()
                .setWechatUserId(wechatUserId)
                .setWebMobileUserId(webMobileUserId)
                .build();
        String uuid = UUID.randomUUID().toString();
        TransactionEventMessage transactionEvent = TransactionEventMessage.newBuilder()
                .setType(TransactionEventType.TRANSACTION_EVENT_MERGE_USER_ACCOUNTS)
                .setEventId(uuid)
                .setTimestamp(System.currentTimeMillis())
                .setMergeUserAccountsEvent(mergeUserAccountsEvent)
                .build();
        return new Message(this.mergerTransactionConfiguration.getTopic(),
                this.mergerTransactionConfiguration.getTag(), uuid, transactionEvent.toByteArray());
    }

    /**
     * Sends a transaction message in async manner.
     *
     * @param message {@link Message}.
     */
    private void sendTransactionAsync(Message message) {
        if (Objects.isNull(message)) {
            return;
        }

        CompletableFuture
                .supplyAsync(() -> this.transactionProducerBean.send(message, this.couaLocalTransactionExecuterService, null),
                        this.executorService)
                .handle((re, thr) -> {
                    if (Objects.nonNull(re)) {
                        return re;
                    }
                    log.warn("Sending message to topic {} error.", message.getTopic());
                    return null;
                });
    }

}
