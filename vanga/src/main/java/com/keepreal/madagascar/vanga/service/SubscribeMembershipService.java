package com.keepreal.madagascar.vanga.service;

import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.common.snowflake.generator.LongIdGenerator;
import com.keepreal.madagascar.vanga.model.Balance;
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
    private final BalanceService balanceService;
    private final PaymentService paymentService;
    private final SkuService membershipSkuService;
    private final SubscribeMembershipRepository subscriptionMemberRepository;
    private final LongIdGenerator idGenerator;
    private final RedissonClient redissonClient;
    private final NotificationEventProducerService notificationEventProducerService;

    /**
     * Constructor the subscribe membership service.
     *
     * @param balanceService                   {@link BalanceService}.
     * @param paymentService                   {@link PaymentService}.
     * @param membershipSkuService             {@link SkuService}.
     * @param subscriptionMemberRepository     {@link SubscribeMembershipRepository}.
     * @param idGenerator                      {@link LongIdGenerator}.
     * @param redissonClient                   {@link RedissonClient}.
     * @param notificationEventProducerService {@link NotificationEventProducerService}.
     */
    public SubscribeMembershipService(BalanceService balanceService, PaymentService paymentService,
                                      SkuService membershipSkuService,
                                      SubscribeMembershipRepository subscriptionMemberRepository,
                                      LongIdGenerator idGenerator,
                                      RedissonClient redissonClient,
                                      NotificationEventProducerService notificationEventProducerService) {
        this.balanceService = balanceService;
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
        return this.subscriptionMemberRepository.getMemberCountByIslandId(islandId, getStartOfDayTime());
    }

    /**
     * retrieve the membership count by membership id.
     *
     * @param membershipId membership id.
     * @return member count.
     */
    public Integer getMemberCountByMembershipId(String membershipId) {
        return this.subscriptionMemberRepository.getMemberCountByMembershipId(membershipId, getStartOfDayTime());
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

        try (AutoRedisLock ignored = new AutoRedisLock(this.redissonClient, String.format("member-wechat-%s", wechatOrder.getUserId()))) {
            List<Payment> innerPaymentList = this.paymentService.retrievePaymentsByOrderId(wechatOrder.getId());

            MembershipSku sku = this.membershipSkuService.retrieveMembershipSkuById(wechatOrder.getMemberShipSkuId());

            if (innerPaymentList.isEmpty()) {
                innerPaymentList = this.paymentService.createNewWechatPayments(wechatOrder, sku);
            } else if (innerPaymentList.stream().allMatch(payment -> PaymentState.OPEN.getValue() == payment.getState())) {
                return;
            }

            Balance hostBalance = this.balanceService.retrieveOrCreateBalanceIfNotExistsByUserId(sku.getHostId());
            hostBalance.setBalanceInCents(hostBalance.getBalanceInCents() + (sku.getPriceInCents() / 100L) * hostBalance.getWithdrawPercent());

            SubscribeMembership currentSubscribeMembership = this.subscriptionMemberRepository.findByUserIdAndMembershipIdAndDeletedIsFalse(
                    wechatOrder.getUserId(), sku.getMembershipId());

            Instant instant = Objects.isNull(currentSubscribeMembership) ?
                    Instant.now() : Instant.ofEpochMilli(currentSubscribeMembership.getExpireTime());
            ZonedDateTime currentExpireTime = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());

            List<Payment> finalInnerPaymentList = innerPaymentList;
            IntStream.range(0, innerPaymentList.size())
                    .forEach(i -> {
                        finalInnerPaymentList.get(i).setWithdrawPercent(hostBalance.getWithdrawPercent());
                        finalInnerPaymentList.get(i).setState(PaymentState.OPEN.getValue());
                        finalInnerPaymentList.get(i).setValidAfter(currentExpireTime
                                .plusMonths(i * SubscribeMembershipService.PAYMENT_SETTLE_IN_MONTH)
                                .toInstant().toEpochMilli());
                    });

            this.balanceService.updateBalance(hostBalance);
            this.paymentService.updateAll(innerPaymentList);
            this.createOrRenewSubscriptionMember(wechatOrder.getUserId(), sku, currentSubscribeMembership, currentExpireTime);
        }
    }

    /**
     * Subscribes membership with shell.
     *
     * @param userId User id.
     * @param sku    {@link MembershipSku}.
     */
    @Transactional
    public void subscribeMembershipWithShell(String userId, MembershipSku sku) {
        try (AutoRedisLock ignored = new AutoRedisLock(this.redissonClient, String.format("member-shell-%s", userId))) {
            Balance userBalance = this.balanceService.retrieveOrCreateBalanceIfNotExistsByUserId(userId);
            if (userBalance.getBalanceInShells() < sku.getPriceInShells()) {
                throw new KeepRealBusinessException(ErrorCode.REQUEST_USER_SHELL_INSUFFICIENT_ERROR);
            }
            userBalance.setBalanceInShells(userBalance.getBalanceInShells() - sku.getPriceInShells());

            SubscribeMembership currentSubscribeMembership = this.subscriptionMemberRepository.findByUserIdAndMembershipIdAndDeletedIsFalse(
                    userId, sku.getMembershipId());
            Instant instant = Objects.isNull(currentSubscribeMembership) ?
                    Instant.now() : Instant.ofEpochMilli(currentSubscribeMembership.getExpireTime());
            ZonedDateTime currentExpireTime = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());

            Balance hostBalance = this.balanceService.retrieveOrCreateBalanceIfNotExistsByUserId(sku.getHostId());
            hostBalance.setBalanceInCents(hostBalance.getBalanceInCents() + (sku.getPriceInShells() / 100L) * hostBalance.getWithdrawPercent());

            this.balanceService.updateBalance(userBalance);
            this.balanceService.updateBalance(hostBalance);
            this.paymentService.createNewShellPayments(userId, hostBalance.getWithdrawPercent(), sku);
            this.createOrRenewSubscriptionMember(userId, sku, currentSubscribeMembership, currentExpireTime);
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
        List<String> membershipIdList = subscriptionMemberRepository.getMembershipIdListByUserIdAndIslandId(userId, islandId, getStartOfDayTime());
        return membershipIdList == null ? Collections.emptyList() : membershipIdList;
    }

    /**
     * Gets the current day start timestamp.
     *
     * @return Timestamp.
     */
    private long getStartOfDayTime() {
        return LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

}