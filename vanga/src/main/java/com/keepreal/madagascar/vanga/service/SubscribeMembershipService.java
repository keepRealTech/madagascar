package com.keepreal.madagascar.vanga.service;

import com.keepreal.madagascar.common.snowflake.generator.LongIdGenerator;
import com.keepreal.madagascar.vanga.model.MembershipSku;
import com.keepreal.madagascar.vanga.model.Payment;
import com.keepreal.madagascar.vanga.model.PaymentState;
import com.keepreal.madagascar.vanga.model.SubscribeMembership;
import com.keepreal.madagascar.vanga.model.WechatOrder;
import com.keepreal.madagascar.vanga.model.WechatOrderState;
import com.keepreal.madagascar.vanga.repository.SubscribeMembershipRepository;
import com.keepreal.madagascar.vanga.util.AutoRedisLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.stream.IntStream;

/**
 * Represents the subscribe membership service.
 */
@Service
public class SubscribeMembershipService {

    private static final long PAYMENT_SETTLE_IN_MONTH = 1L;
    private final PaymentService paymentService;
    private final SkuService membershipSkuService;
    private final SubscribeMembershipRepository subscriptionMemberRepository;
    private final LongIdGenerator idGenerator;
    private final RedissonClient redissonClient;
    private final NotificationEventProducerService notificationEventProducerService;

    /**
     * Constructor the subscribe membership service.
     *
     * @param paymentService                   {@link PaymentService}.
     * @param membershipSkuService             {@link SkuService}.
     * @param subscriptionMemberRepository     {@link SubscribeMembershipRepository}.
     * @param idGenerator                      {@link LongIdGenerator}.
     * @param redissonClient                   {@link RedissonClient}.
     * @param notificationEventProducerService {@link NotificationEventProducerService}.
     */
    public SubscribeMembershipService(PaymentService paymentService,
                                      SkuService membershipSkuService,
                                      SubscribeMembershipRepository subscriptionMemberRepository,
                                      LongIdGenerator idGenerator,
                                      RedissonClient redissonClient,
                                      NotificationEventProducerService notificationEventProducerService) {
        this.paymentService = paymentService;
        this.membershipSkuService = membershipSkuService;
        this.subscriptionMemberRepository = subscriptionMemberRepository;
        this.idGenerator = idGenerator;
        this.redissonClient = redissonClient;
        this.notificationEventProducerService = notificationEventProducerService;
    }

    /**
     * retrieve the membership count by island id.
     *
     * @param islandId island id.
     * @return member count.
     */
    public Integer getMemberCountByIslandId(String islandId) {
        return this.subscriptionMemberRepository.getMemberCountByIslandId(islandId, getCurrentTime());
    }

    /**
     * retrieve the membership count by membership id.
     *
     * @param membershipId membership id.
     * @return member count.
     */
    public Integer getMemberCountByMembershipId(String membershipId) {
        return this.subscriptionMemberRepository.getMemberCountByMembershipId(membershipId, getCurrentTime());
    }

    /**
     * Subscribe member for a newly succeed wechat pay order.
     *
     * @param wechatOrder {@link WechatOrder}.
     */
    @Transactional
    public void subscribeMembershipWithWechatOrder(WechatOrder wechatOrder) {
        if (Objects.isNull(wechatOrder) || WechatOrderState.SUCCESS.getValue() != wechatOrder.getState()) {
            return;
        }

        List<Payment> paymentList = this.paymentService.retrievePaymentsByOrderId(wechatOrder.getId());

        if (paymentList.stream().allMatch(payment -> PaymentState.OPEN.getValue() == payment.getState())) {
            return;
        }

        try (AutoRedisLock ignored = new AutoRedisLock(this.redissonClient, String.format("member-%s", wechatOrder.getUserId()))) {
            List<Payment> innerPaymentList = this.paymentService.retrievePaymentsByOrderId(wechatOrder.getId());

            if (innerPaymentList.stream().allMatch(payment -> PaymentState.OPEN.getValue() == payment.getState())) {
                return;
            }

            MembershipSku sku = this.membershipSkuService.retrieveMembershipSkuById(wechatOrder.getMemberShipSkuId());

            SubscribeMembership currentSubscribeMembership = this.subscriptionMemberRepository.findByUserIdAndMembershipIdAndDeletedIsFalse(
                    wechatOrder.getUserId(), sku.getMembershipId());

            Instant instant = Objects.isNull(currentSubscribeMembership) ?
                    Instant.now() : Instant.ofEpochMilli(currentSubscribeMembership.getExpireTime());
            ZonedDateTime currentExpireTime = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());

            IntStream.range(0, innerPaymentList.size())
                    .forEach(i -> {
                        innerPaymentList.get(i).setState(PaymentState.OPEN.getValue());
                        innerPaymentList.get(i).setValidAfter(currentExpireTime
                                .plusMonths(i * SubscribeMembershipService.PAYMENT_SETTLE_IN_MONTH)
                                .toInstant().toEpochMilli());
                    });
            this.paymentService.updateAll(innerPaymentList);
            this.createOrRenewSubscriptionMember(wechatOrder.getUserId(), sku, currentSubscribeMembership, currentExpireTime);
        }
    }

    /**
     * Creates or renews the membership subscription.
     *
     * @param userId              User id.
     * @param sku                 {@link MembershipSku}.
     * @param subscribeMembership {@link SubscribeMembership}.
     * @param currentExpireTime   {@link ZonedDateTime}.
     */
    @Transactional
    public void createOrRenewSubscriptionMember(String userId,
                                                MembershipSku sku,
                                                SubscribeMembership subscribeMembership,
                                                ZonedDateTime currentExpireTime) {

        long expireTime = currentExpireTime.plusMonths(sku.getTimeInMonths()).toInstant().toEpochMilli();

        if (Objects.nonNull(subscribeMembership)) {
            subscribeMembership.setExpireTime(expireTime);
        } else {
            subscribeMembership = SubscribeMembership.builder()
                    .id(String.valueOf(this.idGenerator.nextId()))
                    .userId(userId)
                    .islandId(sku.getIslandId())
                    .membershipId(sku.getMembershipId())
                    .expireTime(expireTime)
                    .build();
        }

        this.subscriptionMemberRepository.save(subscribeMembership);
        this.notificationEventProducerService.produceNewMemberNotificationEventAsync(subscribeMembership, sku);
    }

    /**
     * retrieve the membership id list by user id and island id.
     *
     * @param userId    user id.
     * @param islandId  island id.
     * @return  membership id list.
     */
    public List<String> getMembershipIdListByUserIdAndIslandId(String userId, String islandId) {
        List<String> membershipIdList = subscriptionMemberRepository.getMembershipIdListByUserIdAndIslandId(userId, islandId, getCurrentTime());
        return membershipIdList == null ? Collections.emptyList() : membershipIdList;
    }

    private long getCurrentTime() {
        return LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}