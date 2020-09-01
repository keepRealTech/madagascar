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
            return Action.CommitMessage;
        } catch (Exception e) {
            return Action.ReconsumeLater;
        }
    }

    @Transactional
    public void mergeUserAccounts(String wechatUserId, String webMobileUserId) {
        this.mergeUserMembership(wechatUserId, webMobileUserId);
        this.mergeUserWechatOrder(wechatUserId, webMobileUserId);
        this.mergeUserPayment(wechatUserId, webMobileUserId);
    }

    /**
     * 合并会员
     *
     * @param wechatUserId      wechat user id
     * @param webMobileUserId   web mobile user id
     */
    @Transactional
    public void mergeUserMembership(String wechatUserId, String webMobileUserId) {
        int page = 0;
        int pageSize = 50;
        Page<SubscribeMembership> memberships = this.subscribeMembershipService.retrieveSubscribeMembershipByUserIdPageable(webMobileUserId, PageRequest.of(page, pageSize));
        int totalPages = memberships.getTotalPages();

        do {
            Page<SubscribeMembership> subscribeMemberships = this.subscribeMembershipService.retrieveSubscribeMembershipByUserIdPageable(webMobileUserId, PageRequest.of(page, pageSize));
            subscribeMemberships.getContent().forEach(membership -> membership.setUserId(wechatUserId));
            this.subscribeMembershipService.updateAll(subscribeMemberships.getContent());
            ++ page;
        } while (page < totalPages);
    }

    /**
     * 合并微信订单
     *
     * @param wechatUserId      wechat user id
     * @param webMobileUserId   web mobile user id
     */
    @Transactional
    public void mergeUserWechatOrder(String wechatUserId, String webMobileUserId) {
        int page = 0;
        int pageSize = 50;
        Page<WechatOrder> wechatOrders = this.wechatOrderService.retrieveByUserIdPageable(webMobileUserId, PageRequest.of(page, pageSize));
        int totalPages = wechatOrders.getTotalPages();

        do {
            Page<WechatOrder> orders = this.wechatOrderService.retrieveByUserIdPageable(webMobileUserId, PageRequest.of(page, pageSize));
            orders.getContent().forEach(wechatOrder -> wechatOrder.setUserId(wechatUserId));
            this.wechatOrderService.updateAll(orders.getContent());
            ++ page;
        } while (page < totalPages);
    }

    /**
     * 合并payment
     *
     * @param wechatUserId      wechat user id
     * @param webMobileUserId   web mobile user id
     */
    @Transactional
    public void mergeUserPayment(String wechatUserId, String webMobileUserId) {
        int page = 0;
        int pageSize = 50;
        Page<Payment> payment = this.paymentService.retrieveAllPaymentsByUserId(webMobileUserId, PageRequest.of(page, pageSize));
        int totalPages = payment.getTotalPages();

        do {
            Page<Payment> payments = this.paymentService.retrieveAllPaymentsByUserId(webMobileUserId, PageRequest.of(page, pageSize));
            payments.getContent().forEach(p -> p.setUserId(wechatUserId));
            this.paymentService.updateAll(payments.getContent());
            ++ page;
        } while (page < totalPages);
    }

}
