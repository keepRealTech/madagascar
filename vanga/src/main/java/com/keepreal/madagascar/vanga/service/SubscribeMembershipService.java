package com.keepreal.madagascar.vanga.service;

import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.common.snowflake.generator.LongIdGenerator;
import com.keepreal.madagascar.vanga.SubscribeMembershipMessage;
import com.keepreal.madagascar.vanga.model.Balance;
import com.keepreal.madagascar.vanga.model.IosOrder;
import com.keepreal.madagascar.vanga.model.MembershipSku;
import com.keepreal.madagascar.vanga.model.Order;
import com.keepreal.madagascar.vanga.model.Payment;
import com.keepreal.madagascar.vanga.model.PaymentState;
import com.keepreal.madagascar.vanga.model.PaymentType;
import com.keepreal.madagascar.vanga.model.SubscribeMembership;
import com.keepreal.madagascar.vanga.model.OrderState;
import com.keepreal.madagascar.vanga.repository.SubscribeMembershipRepository;
import com.keepreal.madagascar.vanga.settlementCalculator.DefaultSettlementCalculator;
import com.keepreal.madagascar.vanga.settlementCalculator.SettlementCalculator;
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

    public static final long PAYMENT_SETTLE_IN_MONTH = 1L;
    private static final long PERMANENT_TIMESTAMP = 4102329600000L;
    private final IOSOrderService iosOrderService;
    private final BalanceService balanceService;
    private final PaymentService paymentService;
    private final SkuService membershipSkuService;
    private final SubscribeMembershipRepository subscriptionMemberRepository;
    private final LongIdGenerator idGenerator;
    private final RedissonClient redissonClient;
    private final NotificationEventProducerService notificationEventProducerService;
    private final IncomeService incomeService;

    private final SettlementCalculator settlementCalculator;

    /**
     * Constructor the subscribe membership service.
     *
     * @param iosOrderService                  {@link IOSOrderService}.
     * @param balanceService                   {@link BalanceService}.
     * @param paymentService                   {@link PaymentService}.
     * @param membershipSkuService             {@link SkuService}.
     * @param subscriptionMemberRepository     {@link SubscribeMembershipRepository}.
     * @param idGenerator                      {@link LongIdGenerator}.
     * @param redissonClient                   {@link RedissonClient}.
     * @param notificationEventProducerService {@link NotificationEventProducerService}.
     * @param incomeService                    {@link IncomeService}.
     */
    public SubscribeMembershipService(IOSOrderService iosOrderService,
                                      BalanceService balanceService,
                                      PaymentService paymentService,
                                      SkuService membershipSkuService,
                                      SubscribeMembershipRepository subscriptionMemberRepository,
                                      LongIdGenerator idGenerator,
                                      RedissonClient redissonClient,
                                      NotificationEventProducerService notificationEventProducerService,
                                      IncomeService incomeService) {
        this.iosOrderService = iosOrderService;
        this.balanceService = balanceService;
        this.paymentService = paymentService;
        this.membershipSkuService = membershipSkuService;
        this.subscriptionMemberRepository = subscriptionMemberRepository;
        this.idGenerator = idGenerator;
        this.redissonClient = redissonClient;
        this.notificationEventProducerService = notificationEventProducerService;
        this.incomeService = incomeService;

        this.settlementCalculator = new DefaultSettlementCalculator();
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
     * @param order       {@link Order}.
     * @param paymentType {@link PaymentType}.
     */
    @Transactional
    public void subscribeMembershipWithOrder(Order order, PaymentType paymentType) {
        if (Objects.isNull(order) || OrderState.SUCCESS.getValue() != order.getState()) {
            return;
        }

        List<Payment> paymentList = this.paymentService.retrievePaymentsByOrderId(order.getId());

        if (paymentList.stream().allMatch(payment -> PaymentState.DRAFTED.getValue() != payment.getState())) {
            return;
        }

        try (AutoRedisLock ignored = new AutoRedisLock(this.redissonClient, String.format("member-%s", order.getUserId()))) {
            List<Payment> innerPaymentList = this.paymentService.retrievePaymentsByOrderId(order.getId());

            MembershipSku sku = this.membershipSkuService.retrieveMembershipSkuById(order.getPropertyId());

            if (innerPaymentList.isEmpty()) {
                innerPaymentList = this.paymentService.createNewWechatMembershipPayments(order, sku, paymentType);
            } else if (innerPaymentList.stream().allMatch(payment -> PaymentState.OPEN.getValue() == payment.getState())) {
                return;
            }

            Balance hostBalance = this.balanceService.retrieveOrCreateBalanceIfNotExistsByUserId(sku.getHostId());

            SubscribeMembership currentSubscribeMembership = this.subscriptionMemberRepository.findByUserIdAndMembershipIdAndDeletedIsFalse(
                    order.getUserId(), sku.getMembershipId());

            Instant instant = Objects.isNull(currentSubscribeMembership) || currentSubscribeMembership.getExpireTime() < System.currentTimeMillis() ?
                    Instant.now() : Instant.ofEpochMilli(currentSubscribeMembership.getExpireTime());
            ZonedDateTime currentExpireTime = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());

            List<Payment> finalInnerPaymentList = innerPaymentList;
            IntStream.range(0, innerPaymentList.size())
                    .forEach(i -> {
                        finalInnerPaymentList.get(i).setWithdrawPercent(hostBalance.getWithdrawPercent());
                        finalInnerPaymentList.get(i).setState(PaymentState.OPEN.getValue());
                        finalInnerPaymentList.get(i).setValidAfter(this.settlementCalculator.generateSettlementTimestamp(currentExpireTime, i));
                    });

            this.balanceService.addOnCents(hostBalance, this.calculateAmount(sku.getPriceInCents(), hostBalance.getWithdrawPercent()));
            this.paymentService.updateAll(innerPaymentList);
            this.createOrRenewSubscriptionMember(order.getUserId(), sku, currentSubscribeMembership, currentExpireTime);
            this.incomeService.updateIncomeAll(sku.getHostId(), order.getUserId(), System.currentTimeMillis(), sku.getPriceInCents());
        }
    }

    /**
     * Subscribes membership with shell.
     *
     * @param userId User id.
     * @param sku    {@link MembershipSku}.
     */
    @Deprecated
    @Transactional
    public void subscribeMembershipWithShell(String userId, MembershipSku sku) {
        try (AutoRedisLock ignored = new AutoRedisLock(this.redissonClient, String.format("member-%s", userId))) {
            SubscribeMembership currentSubscribeMembership = this.subscriptionMemberRepository.findByUserIdAndMembershipIdAndDeletedIsFalse(
                    userId, sku.getMembershipId());
            Instant instant = Objects.isNull(currentSubscribeMembership) || currentSubscribeMembership.getExpireTime() < System.currentTimeMillis() ?
                    Instant.now() : Instant.ofEpochMilli(currentSubscribeMembership.getExpireTime());
            ZonedDateTime currentExpireTime = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());

            Balance userBalance = this.balanceService.retrieveOrCreateBalanceIfNotExistsByUserId(userId);

            if (userBalance.getFrozen()) {
                throw new KeepRealBusinessException(ErrorCode.REQUEST_USER_BALANCE_HAS_BEEN_FROZEN);
            }

            Balance hostBalance = this.balanceService.retrieveOrCreateBalanceIfNotExistsByUserId(sku.getHostId());
            this.balanceService.consumeShells(userBalance, sku.getPriceInShells());
            this.balanceService.addOnCents(hostBalance, this.calculateAmount(sku.getPriceInCents(), hostBalance.getWithdrawPercent()));
            this.paymentService.createPayShellPayments(userId, hostBalance.getWithdrawPercent(), sku, currentExpireTime);
            this.createOrRenewSubscriptionMember(userId, sku, currentSubscribeMembership, currentExpireTime);
            this.incomeService.updateIncomeAll(sku.getHostId(), userId, System.currentTimeMillis(), sku.getPriceInCents());

        }
    }

    /**
     * Subscribes membership with ios pay.
     *
     * @param userId        User id.
     * @param receipt       Ios receipt.
     * @param transactionId Transaction id.
     * @param sku           Membership sku.
     */
    @Deprecated
    @Transactional
    public void subscribeMembershipWithIOSOrder(String userId, String receipt, String transactionId, MembershipSku sku) {
        IosOrder iosOrder = this.iosOrderService.verify(userId, receipt, sku.getDescription(), sku.getAppleSkuId(), sku.getId(), transactionId);
        try (AutoRedisLock ignored = new AutoRedisLock(this.redissonClient, String.format("member-%s", userId))) {
            SubscribeMembership currentSubscribeMembership = this.subscriptionMemberRepository.findByUserIdAndMembershipIdAndDeletedIsFalse(
                    userId, sku.getMembershipId());
            Instant instant = Objects.isNull(currentSubscribeMembership) || currentSubscribeMembership.getExpireTime() < System.currentTimeMillis() ?
                    Instant.now() : Instant.ofEpochMilli(currentSubscribeMembership.getExpireTime());
            ZonedDateTime currentExpireTime = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());

            Balance hostBalance = this.balanceService.retrieveOrCreateBalanceIfNotExistsByUserId(sku.getHostId());
            this.balanceService.addOnCents(hostBalance, this.calculateAmount(sku.getPriceInCents(), hostBalance.getWithdrawPercent()));
            this.paymentService.createIOSPayPayments(userId, iosOrder, hostBalance.getWithdrawPercent(), sku, currentExpireTime);
            this.createOrRenewSubscriptionMember(userId, sku, currentSubscribeMembership, currentExpireTime);
            this.incomeService.updateIncomeAll(sku.getHostId(), userId, System.currentTimeMillis(), sku.getPriceInCents());

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
        long expireTime = sku.getPermanent() || currentExpireTime.toInstant().toEpochMilli() == PERMANENT_TIMESTAMP ?
                PERMANENT_TIMESTAMP : currentExpireTime.plusMonths(sku.getTimeInMonths()).toInstant().toEpochMilli();

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
     * @param userId   user id.
     * @param islandId island id.
     * @return membership id list.
     */
    public List<String> getMembershipIdListByUserIdAndIslandId(String userId, String islandId) {
        return this.subscriptionMemberRepository.getMembershipIdListByUserIdAndIslandId(userId, islandId, getStartOfDayTime());
    }

    /**
     * Retrieves the membership ids by valid user subscriptions.
     *
     * @param userId User id.
     * @return Membership id list.
     */
    public List<String> getMembershipIdListByUserId(String userId) {
        return this.subscriptionMemberRepository.getMembershipIdListByUserId(userId, this.getStartOfDayTime());
    }

    /**
     * merge user subscribed membership
     *
     * @param wechatUserId    wechat user id
     * @param webMobileUserId mobile user id
     */
    public void mergeUserSubscribeMembership(String wechatUserId, String webMobileUserId) {
        this.subscriptionMemberRepository.mergeUserSubscribeMembership(wechatUserId, webMobileUserId);
    }

    /**
     * Retrieve subscribeMembership by user id and island id.
     *
     * @param userId   user id.
     * @param islandId island id.
     * @return {@link SubscribeMembership}.
     */
    public List<SubscribeMembership> retrieveSubscribeMembership(String userId, String islandId) {
        return this.subscriptionMemberRepository.findSubscribeMembershipsByUserIdAndIslandId(userId, islandId);
    }

    /**
     * Build subscribeMembershipMessage.
     *
     * @param subscribeMembership {@link SubscribeMembership}.
     * @return {@link SubscribeMembershipMessage}.
     */
    public SubscribeMembershipMessage getMessage(SubscribeMembership subscribeMembership) {
        return SubscribeMembershipMessage.newBuilder()
                .setUserId(subscribeMembership.getUserId())
                .setIslandId(subscribeMembership.getIslandId())
                .setMembershipId(subscribeMembership.getMembershipId())
                .setExpiredTime(subscribeMembership.getExpireTime())
                .build();
    }

    /**
     * Gets the current day start timestamp.
     *
     * @return Timestamp.
     */
    private long getStartOfDayTime() {
        return LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    /**
     * Calculates the amount after withdraw ratio.
     *
     * @param amount Amount.
     * @param ratio  Ratio.
     * @return Final amount.
     */
    private Long calculateAmount(Long amount, int ratio) {
        assert amount > 0;
        return amount * ratio / 100L;
    }

}
