package com.keepreal.madagascar.vanga.service;

import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.common.snowflake.generator.LongIdGenerator;
import com.keepreal.madagascar.vanga.model.Balance;
import com.keepreal.madagascar.vanga.model.IosOrder;
import com.keepreal.madagascar.vanga.model.MembershipSku;
import com.keepreal.madagascar.vanga.model.Order;
import com.keepreal.madagascar.vanga.model.Payment;
import com.keepreal.madagascar.vanga.model.PaymentState;
import com.keepreal.madagascar.vanga.model.PaymentType;
import com.keepreal.madagascar.vanga.model.ShellSku;
import com.keepreal.madagascar.vanga.model.WechatOrder;
import com.keepreal.madagascar.vanga.repository.PaymentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Represents the payment service.
 */
@Service
public class PaymentService {

    private final LongIdGenerator idGenerator;
    private final BalanceService balanceService;
    private final PaymentRepository paymentRepository;

    /**
     * Constructs the payment service.
     *
     * @param idGenerator       {@link LongIdGenerator}.
     * @param balanceService    {@link BalanceService}.
     * @param paymentRepository {@link PaymentRepository}.
     */
    public PaymentService(LongIdGenerator idGenerator,
                          BalanceService balanceService,
                          PaymentRepository paymentRepository) {
        this.idGenerator = idGenerator;
        this.balanceService = balanceService;
        this.paymentRepository = paymentRepository;
    }

    /**
     * Creates a withdraw payment if comply to all constraints.
     *
     * @param userId        User id.
     * @param amountInCents Amount in cents to withdraw.
     * @return {@link Balance}.
     */
    @Transactional
    public Balance createWithdrawPayment(String userId, Long amountInCents) {
        Balance balance = this.balanceService.retrieveOrCreateBalanceIfNotExistsByUserId(userId);
        balance = this.balanceService.withdraw(balance, amountInCents);

        ZonedDateTime todayStart = ZonedDateTime.now().toLocalDate().atStartOfDay(ZoneId.systemDefault());

        List<Payment> payments = this.paymentRepository.findAllByTypeAndCreatedTimeAfterAndDeletedIsFalse(
                PaymentType.WITHDRAW.getValue(), todayStart.toInstant().toEpochMilli());

        Long current = payments.stream().map(Payment::getAmountInCents).reduce(0L, Long::sum);
        if (current + amountInCents > balance.getWithdrawDayLimitInCents()) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_USER_BALANCE_WITHDRAW_DAY_LIMIT_ERROR);
        }

        Payment payment = Payment.builder()
                .id(String.valueOf(this.idGenerator.nextId()))
                .userId(userId)
                .state(PaymentState.OPEN.getValue())
                .amountInCents(amountInCents)
                .type(PaymentType.WITHDRAW.getValue())
                .withdrawPercent(100)
                .build();

        this.paymentRepository.save(payment);

        return balance;
    }

    /**
     * Creates new wechat payments for given order.
     *
     * @param order       {@link Order}.
     * @param sku         {@link MembershipSku}.
     * @param paymentType {@link PaymentType}.
     * @return {@link Payment}.
     */
    @Transactional
    public List<Payment> createNewWechatMembershipPayments(Order order, MembershipSku sku, PaymentType paymentType) {
        Balance payeeBalance = this.balanceService.retrieveOrCreateBalanceIfNotExistsByUserId(sku.getHostId());
        List<Payment> payments =
                IntStream.range(0, sku.getTimeInMonths())
                        .mapToObj(i -> Payment.builder()
                                .id(String.valueOf(this.idGenerator.nextId()))
                                .type(paymentType.getValue())
                                .amountInCents(sku.getPriceInCents() / sku.getTimeInMonths())
                                .userId(order.getUserId())
                                .state(PaymentState.DRAFTED.getValue())
                                .payeeId(sku.getHostId())
                                .orderId(order.getId())
                                .tradeNum(order.getTradeNumber())
                                .membershipSkuId(order.getPropertyId())
                                .withdrawPercent(payeeBalance.getWithdrawPercent())
                                .build())
                        .collect(Collectors.toList());

        return this.paymentRepository.saveAll(payments);
    }

    /**
     * Creates new wechat payments for given order.
     *
     * @param wechatOrder  {@link WechatOrder}.
     * @param hostId       Host id.
     * @param priceIncents Price in cents.
     * @return {@link Payment}.
     */
    @Transactional
    public Payment createNewWechatQuestionFeedPayment(WechatOrder wechatOrder, String hostId, long priceIncents) {
        Balance payeeBalance = this.balanceService.retrieveOrCreateBalanceIfNotExistsByUserId(hostId);
        Payment payment = Payment.builder()
                .id(String.valueOf(this.idGenerator.nextId()))
                .type(PaymentType.WECHATPAY.getValue())
                .amountInCents(priceIncents)
                .userId(wechatOrder.getUserId())
                .state(PaymentState.DRAFTED.getValue())
                .payeeId(hostId)
                .orderId(wechatOrder.getId())
                .tradeNum(wechatOrder.getTradeNumber())
                .withdrawPercent(payeeBalance.getWithdrawPercent())
                .build();

        return this.paymentRepository.save(payment);
    }

    /**
     * Creates the payments for one time support.
     *
     * @param order        {@link Order}.
     * @param payeeId      Payee id.
     * @param priceInCents Price in cents.
     * @return {@link Payment}.
     */
    @Transactional
    public Payment createNewSupportPayment(Order order, String payeeId, long priceInCents) {
        Balance payeeBalance = this.balanceService.retrieveOrCreateBalanceIfNotExistsByUserId(payeeId);
        Payment payment = Payment.builder()
                .id(String.valueOf(this.idGenerator.nextId()))
                .type(PaymentType.SUPPORT.getValue())
                .amountInCents(priceInCents)
                .userId(order.getUserId())
                .state(PaymentState.DRAFTED.getValue())
                .payeeId(payeeId)
                .orderId(order.getId())
                .tradeNum(order.getTradeNumber())
                .withdrawPercent(payeeBalance.getWithdrawPercent())
                .build();

        return this.paymentRepository.save(payment);
    }

    @Transactional
    public Payment createNewFeedChargePayment(Order order, String payeeId, long priceInCents) {
        Balance payeeBalance = this.balanceService.retrieveOrCreateBalanceIfNotExistsByUserId(payeeId);
        Payment payment = Payment.builder()
                .id(String.valueOf(this.idGenerator.nextId()))
                .type(PaymentType.WECHATPAY.getValue())
                .amountInCents(priceInCents)
                .userId(order.getUserId())
                .state(PaymentState.DRAFTED.getValue())
                .payeeId(payeeId)
                .orderId(order.getId())
                .tradeNum(order.getTradeNumber())
                .withdrawPercent(payeeBalance.getWithdrawPercent())
                .build();

        return this.paymentRepository.save(payment);
    }

    /**
     * Creates new shell payments.
     *
     * @param userId            User id.
     * @param withdrawPercent   Withdraw percent.
     * @param sku               {@link MembershipSku}.
     * @param currentExpireTime {@link ZonedDateTime}.
     * @return {@link Payment}.
     */
    @Deprecated
    @Transactional
    public List<Payment> createPayShellPayments(String userId, Integer withdrawPercent, MembershipSku sku, ZonedDateTime currentExpireTime) {
        String tradeNum = UUID.randomUUID().toString().replace("-", "");
        List<Payment> payments =
                IntStream.range(0, sku.getTimeInMonths())
                        .mapToObj(i -> Payment.builder()
                                .id(String.valueOf(this.idGenerator.nextId()))
                                .type(PaymentType.SHELLPAY.getValue())
                                .amountInShells(sku.getPriceInShells() / sku.getTimeInMonths())
                                .userId(userId)
                                .withdrawPercent(withdrawPercent)
                                .state(PaymentState.OPEN.getValue())
                                .payeeId(sku.getHostId())
                                .validAfter(currentExpireTime
                                        .plusMonths((i + 1) * SubscribeMembershipService.PAYMENT_SETTLE_IN_MONTH)
                                        .toInstant().toEpochMilli())
                                .membershipSkuId(sku.getId())
                                .tradeNum(tradeNum)
                                .build())
                        .collect(Collectors.toList());

        return this.paymentRepository.saveAll(payments);
    }

    /**
     * Creates new shell payments.
     *
     * @param userId            User id.
     * @param iosOrder          {@link IosOrder}.
     * @param withdrawPercent   Withdraw percent.
     * @param sku               {@link MembershipSku}.
     * @param currentExpireTime {@link ZonedDateTime}.
     * @return {@link Payment}.
     */
    @Deprecated
    @Transactional
    public List<Payment> createIOSPayPayments(String userId,
                                              IosOrder iosOrder,
                                              Integer withdrawPercent,
                                              MembershipSku sku,
                                              ZonedDateTime currentExpireTime) {
        Payment payment = this.paymentRepository.findTopByTradeNumAndTypeAndDeletedIsFalse(iosOrder.getTransactionId(), PaymentType.IOSBUY.getValue());

        if (Objects.nonNull(payment)) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_USER_SHELL_IOS_RECEIPT_DUPLICATE_ERROR);
        }

        List<Payment> payments =
                IntStream.range(0, sku.getTimeInMonths())
                        .mapToObj(i -> Payment.builder()
                                .id(String.valueOf(this.idGenerator.nextId()))
                                .type(PaymentType.IOSBUY.getValue())
                                .amountInShells(sku.getPriceInShells() / sku.getTimeInMonths())
                                .userId(userId)
                                .withdrawPercent(withdrawPercent)
                                .state(PaymentState.OPEN.getValue())
                                .payeeId(sku.getHostId())
                                .validAfter(currentExpireTime
                                        .plusMonths((i + 1) * SubscribeMembershipService.PAYMENT_SETTLE_IN_MONTH)
                                        .toInstant().toEpochMilli())
                                .membershipSkuId(sku.getId())
                                .tradeNum(iosOrder.getTransactionId())
                                .orderId(iosOrder.getId())
                                .build())
                        .collect(Collectors.toList());

        return this.paymentRepository.saveAll(payments);
    }

    /**
     * Creates new buy shell payments.
     *
     * @param userId        User id.
     * @param sku           {@link ShellSku}.
     * @param transactionId Transaction id.
     * @return {@link Payment}.
     */
    @Transactional
    public Payment createIOSBuyShellPayments(String userId, ShellSku sku, String transactionId, String orderId) {
        Payment payment = this.paymentRepository.findTopByTradeNumAndTypeAndDeletedIsFalse(transactionId, PaymentType.SHELLBUY.getValue());

        if (Objects.nonNull(payment)) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_USER_SHELL_IOS_RECEIPT_DUPLICATE_ERROR);
        }

        payment = Payment.builder()
                .id(String.valueOf(this.idGenerator.nextId()))
                .type(PaymentType.SHELLBUY.getValue())
                .amountInShells(sku.getShells())
                .amountInCents(sku.getPriceInCents())
                .userId(userId)
                .state(PaymentState.CLOSED.getValue())
                .tradeNum(transactionId)
                .orderId(orderId)
                .build();
        return this.paymentRepository.save(payment);
    }

    /**
     * Creates new buy shell payments.
     *
     * @param wechatOrder {@link WechatOrder}.
     * @param sku         {@link ShellSku}.
     * @return {@link Payment}.
     */
    @Transactional
    public Payment createWechatBuyShellPayments(WechatOrder wechatOrder, ShellSku sku) {
        Payment payment = this.paymentRepository.findTopByTradeNumAndTypeAndDeletedIsFalse(wechatOrder.getTradeNumber(),
                PaymentType.SHELLBUY.getValue());

        if (Objects.nonNull(payment)) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_USER_SHELL_IOS_RECEIPT_DUPLICATE_ERROR);
        }

        payment = Payment.builder()
                .id(String.valueOf(this.idGenerator.nextId()))
                .type(PaymentType.SHELLBUY.getValue())
                .amountInShells(sku.getShells())
                .amountInCents(sku.getPriceInCents())
                .userId(wechatOrder.getUserId())
                .state(PaymentState.DRAFTED.getValue())
                .tradeNum(wechatOrder.getTradeNumber())
                .orderId(wechatOrder.getId())
                .build();
        return this.paymentRepository.save(payment);
    }

    /**
     * Retrieves all payments associates to an order.
     *
     * @param orderId Order id.
     * @return {@link Payment}.
     */
    @Transactional
    public List<Payment> retrievePaymentsByOrderId(String orderId) {
        return this.paymentRepository.findAllByOrderIdAndDeletedIsFalse(orderId);
    }

    /**
     * Updates the payments.
     *
     * @param payments {@link Payment}.
     */
    @Transactional
    public void updateAll(Iterable<Payment> payments) {
        this.paymentRepository.saveAll(payments);
    }

    /**
     * Retrieves valid user payments.
     *
     * @param userId   User id.
     * @param pageable {@link Pageable}.
     * @return {@link Payment}.
     */
    public Page<Payment> retrievePaymentsByUserId(String userId, Pageable pageable) {
        return this.paymentRepository.findAllValidPaymentsByUserId(userId, pageable);
    }

    /**
     * Retrieves valid user withdraws history.
     *
     * @param userId   User id.
     * @param pageable {@link Pageable}.
     * @return {@link Payment}.
     */
    public Page<Payment> retrieveWithdrawsByUserId(String userId, Pageable pageable) {
        return this.paymentRepository.findAllByTypeAndUserIdAndDeletedIsFalse(PaymentType.WITHDRAW.getValue(), userId, pageable);
    }

    /**
     * merge user payment
     *
     * @param wechatUserId    wechat user id
     * @param webMobileUserId mobile user id
     */
    public void mergeUserPayment(String wechatUserId, String webMobileUserId) {
        this.paymentRepository.mergeUserPayment(wechatUserId, webMobileUserId);
    }

    public int supportCount(String userId) {
        Integer count = this.paymentRepository.countByPayeeIdAndTypeAndStateInAndDeletedIsFalse(userId, PaymentType.SUPPORT.getValue(), Arrays.asList(PaymentState.OPEN.getValue(), PaymentState.CLOSED.getValue()));
        return count == null ? 0 : count;
    }

    /**
     * retrieve support count by userId and timestamp
     *
     * @param userId    userId.
     * @param startTimestamp    start timestamp.
     * @param endTimestamp      end timestamp.
     * @return  support count.
     */
    public int retrieveSupportCount(String userId, long startTimestamp, long endTimestamp) {
        return this.paymentRepository.countSupportCountByPayeeIdAndTimestamp(userId, startTimestamp, endTimestamp);
    }

    /**
     * retrieve total cents by userId and timestamp
     *
     * @param userId    userId.
     * @param startTimestamp    start timestamp.
     * @param endTimestamp      end timestamp.
     * @return  total cents.
     */
    public long retrieveCents(String userId, long startTimestamp, long endTimestamp) {
        Long cents = this.paymentRepository.countAmountByPayeeIdAndTimestamp(userId, startTimestamp, endTimestamp);

        return cents == null ? 0 : cents;
    }

    public Page<Payment> retrievePaymentsByPayeeId(String payeeId, long startTimestamp, long endTimestamp, Pageable pageable) {
        return this.paymentRepository.findPaymentsByPayeeIdAndCreatedTimeBetweenAndStateInAndTypeNotAndDeletedIsFalse(payeeId, startTimestamp, endTimestamp, Arrays.asList(2, 3), 4, pageable);
    }

    public Page<Payment> retrieveMembershipPaymentsByPayeeId(String payeeId, long startTimestamp, long endTimestamp, List<String> membershipSkuIds, Pageable pageable) {
        return this.paymentRepository.findPaymentsByPayeeIdAndCreatedTimeBetweenAndStateInAndMembershipSkuIdIn(payeeId, startTimestamp, endTimestamp, Arrays.asList(2, 3), membershipSkuIds, pageable);
    }

    public Page<Payment> retrieveSponsorPaymentsByPayeeId(String payeeId, long startTimestamp, long endTimestamp, Pageable pageable) {
        return this.paymentRepository.findPaymentsByPayeeIdAndCreatedTimeBetweenAndStateInAndType(payeeId, startTimestamp, endTimestamp, Arrays.asList(2, 3), 6, pageable);
    }

    public Page<Payment> retrieveFeedChargePaymentsByPayeeId(String payeeId, long startTimestamp, long endTimestamp, Pageable pageable) {
        return this.paymentRepository.findPaymentsByPayeeIdAndCreatedTimeBetweenAndStateInAndTypeInAndMembershipSkuId(payeeId, startTimestamp, endTimestamp, Arrays.asList(2, 3), Arrays.asList(1, 7), "", pageable);
    }
}