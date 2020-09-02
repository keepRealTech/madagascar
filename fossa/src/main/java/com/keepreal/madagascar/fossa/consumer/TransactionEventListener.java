package com.keepreal.madagascar.fossa.consumer;

import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.MessageListener;
import com.google.protobuf.InvalidProtocolBufferException;
import com.keepreal.madagascar.coua.MergeUserAccountsEvent;
import com.keepreal.madagascar.coua.TransactionEventMessage;
import com.keepreal.madagascar.coua.TransactionEventType;
import com.keepreal.madagascar.fossa.service.FeedInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * represents transaction consumer
 */
@Slf4j
@Component
public class TransactionEventListener implements MessageListener {

    private final FeedInfoService feedInfoService;

    public TransactionEventListener(FeedInfoService feedInfoService) {
        this.feedInfoService = feedInfoService;
    }

    @Override
    public Action consume(Message message, ConsumeContext context) {
        try {
            if (Objects.isNull(message) || Objects.isNull(message.getBody())) {
                return Action.CommitMessage;
            }

            TransactionEventMessage transactionEventMessage = TransactionEventMessage.parseFrom(message.getBody());

            if (transactionEventMessage.getType().equals(TransactionEventType.TRANSACTION_EVENT_MERGE_USER_ACCOUNTS)) {
                MergeUserAccountsEvent mergeUserAccountsEvent = transactionEventMessage.getMergeUserAccountsEvent();
                String wechatUserId = mergeUserAccountsEvent.getWechatUserId();
                String webMobileUserId = mergeUserAccountsEvent.getWebMobileUserId();
                this.feedInfoService.mergeUserBoxInfo(wechatUserId, webMobileUserId);
                return Action.CommitMessage;
            }
            return Action.CommitMessage;
        } catch (InvalidProtocolBufferException e) {
            log.warn("Bad formatted transaction event, skipped.");
            return Action.ReconsumeLater;
        } catch (Exception e) {
            return Action.ReconsumeLater;
        }
    }

}
