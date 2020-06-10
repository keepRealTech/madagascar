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
import java.time.ZonedDateTime;
import java.util.List;
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

    /**
     * Constructor the subscribe membership service.
     *
     * @param paymentService               {@link PaymentService}.
     * @param membershipSkuService         {@link SkuService}.
     * @param subscriptionMemberRepository {@link SubscribeMembershipRepository}.
     * @param idGenerator                  {@link LongIdGenerator}.
     * @param redissonClient               {@link RedissonClient}.
     */
    public SubscribeMembershipService(PaymentService paymentService,
                                      SkuService membershipSkuService,
                                      SubscribeMembershipRepository subscriptionMemberRepository,
                                      LongIdGenerator idGenerator,
                                      RedissonClient redissonClient) {
        this.paymentService = paymentService;
        this.membershipSkuService = membershipSkuService;
        this.subscriptionMemberRepository = subscriptionMemberRepository;
        this.idGenerator = idGenerator;
        this.redissonClient = redissonClient;
    }

    /**
     * retrieve the membership count by island id.
     *
     * @param islandId island id.
     * @return member count.
     */
    public Integer getMemberCountByIslandId(String islandId) {
        LocalDate localDate = LocalDate.now().plusDays(1L);
        long deadline = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        return this.subscriptionMemberRepository.getMemberCountByIslandId(islandId, deadline);
    }

    /**
     * retrieve the membership count by membership id.
     *
     * @param membershipId membership id.
     * @return member count.
     */
    public Integer getMemberCountByMembershipId(String membershipId) {
        LocalDate localDate = LocalDate.now().plusDays(1L);
        long deadline = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        return this.subscriptionMemberRepository.getMemberCountByMembershipId(membershipId, deadline);
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

            SubscribeMembership subscribeMembership = this.subscriptionMemberRepository.findByUserIdAndMembershipIdAndDeletedIsFalse(
                    wechatOrder.getUserId(), wechatOrder.getMemberShipSkuId());

            Instant instant = Objects.isNull(subscribeMembership) ?
                    Instant.now() : Instant.ofEpochSecond(subscribeMembership.getExpireTime());
            ZonedDateTime currentExpireTime = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());

            IntStream.range(0, innerPaymentList.size())
                    .forEach(i -> {
                        innerPaymentList.get(i).setState(PaymentState.OPEN.getValue());
                        innerPaymentList.get(i).setValidAfter(currentExpireTime
                                .plusMonths(i * SubscribeMembershipService.PAYMENT_SETTLE_IN_MONTH)
                                .toInstant().toEpochMilli());
                    });
            this.paymentService.updateAll(innerPaymentList);
            this.createOrRenewSubscriptionMember(wechatOrder, subscribeMembership, currentExpireTime);
        }
    }

    /**
     * Creates or renews the membership subscription.
     *
     * @param wechatOrder         {@link WechatOrder}.
     * @param subscribeMembership {@link SubscribeMembership}.
     * @param currentExpireTime   {@link ZonedDateTime}.
     */
    @Transactional
    public void createOrRenewSubscriptionMember(WechatOrder wechatOrder,
                                                SubscribeMembership subscribeMembership,
                                                ZonedDateTime currentExpireTime) {
        MembershipSku sku = this.membershipSkuService.retrieveMembershipSkuById(wechatOrder.getMemberShipSkuId());

        long expireTime = currentExpireTime.plusMonths(sku.getTimeInMonths()).toInstant().toEpochMilli();

        if (Objects.nonNull(subscribeMembership)) {
            subscribeMembership.setExpireTime(expireTime);
        } else {
            subscribeMembership = SubscribeMembership.builder()
                    .id(String.valueOf(this.idGenerator.nextId()))
                    .userId(wechatOrder.getUserId())
                    .islandId(sku.getIslandId())
                    .membershipId(sku.getMembershipId())
                    .expireTime(expireTime)
                    .build();
        }

        this.subscriptionMemberRepository.save(subscribeMembership);
    }

}