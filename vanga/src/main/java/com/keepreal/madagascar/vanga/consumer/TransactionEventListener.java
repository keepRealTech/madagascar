package com.keepreal.madagascar.vanga.consumer;

import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.MessageListener;

import com.google.protobuf.InvalidProtocolBufferException;
import com.keepreal.madagascar.coua.MergeUserAccountsEvent;
import com.keepreal.madagascar.coua.TransactionEventMessage;
import com.keepreal.madagascar.coua.TransactionEventType;
import com.keepreal.madagascar.vanga.model.Payment;
import com.keepreal.madagascar.vanga.model.SubscribeMembership;
import com.keepreal.madagascar.vanga.model.WechatOrder;
import com.keepreal.madagascar.vanga.service.PaymentService;
import com.keepreal.madagascar.vanga.service.SubscribeMembershipService;
import com.keepreal.madagascar.vanga.service.WechatOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Component
@Slf4j
public class TransactionEventListener implements MessageListener {

    private final WechatOrderService wechatOrderService;
    private final SubscribeMembershipService subscribeMembershipService;
    private final PaymentService paymentService;

    public TransactionEventListener(WechatOrderService wechatOrderService,
                                           SubscribeMembershipService subscribeMembershipService,
                                           PaymentService paymentService) {
        this.wechatOrderService = wechatOrderService;
        this.subscribeMembershipService = subscribeMembershipService;
        this.paymentService = paymentService;
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
                this.mergeUserAccounts(wechatUserId, webMobileUserId);
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

    @Transactional
    public void mergeUserAccounts(String wechatUserId, String webMobileUserId) {
        this.subscribeMembershipService.mergeUserSubscribeMembership(wechatUserId, webMobileUserId);
        this.wechatOrderService.mergeUserWechatOrder(wechatUserId, webMobileUserId);
        this.paymentService.mergeUserPayment(wechatUserId, webMobileUserId);
    }

}
