package com.keepreal.madagascar.vanga.service;

import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.common.snowflake.generator.LongIdGenerator;
import com.keepreal.madagascar.vanga.model.Balance;
import com.keepreal.madagascar.vanga.model.MembershipSku;
import com.keepreal.madagascar.vanga.model.Payment;
import com.keepreal.madagascar.vanga.model.PaymentState;
import com.keepreal.madagascar.vanga.model.PaymentType;
import com.keepreal.madagascar.vanga.model.ShellSku;
import com.keepreal.madagascar.vanga.model.WechatOrder;
import com.keepreal.madagascar.vanga.repository.PaymentRepository;
import com.keepreal.madagascar.vanga.util.PaginationUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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
                .build();

        this.paymentRepository.save(payment);

        return balance;
    }

    /**
     * Creates new wechat payments for given order.
     *
     * @param wechatOrder {@link WechatOrder}.
     * @param sku         {@link MembershipSku}.
     * @return {@link Payment}.
     */
    @Transactional
    public List<Payment> createNewWechatPayments(WechatOrder wechatOrder, MembershipSku sku) {
        List<Payment> payments =
                IntStream.range(0, sku.getTimeInMonths())
                        .mapToObj(i -> Payment.builder()
                                .id(String.valueOf(this.idGenerator.nextId()))
                                .type(PaymentType.WECHATPAY.getValue())
                                .amountInCents(sku.getPriceInCents() / sku.getTimeInMonths())
                                .userId(wechatOrder.getUserId())
                                .state(PaymentState.DRAFTED.getValue())
                                .payeeId(sku.getHostId())
                                .orderId(wechatOrder.getId())
                                .tradeNum(wechatOrder.getTradeNumber())
                                .membershipSkuId(wechatOrder.getMemberShipSkuId())
                                .build())
                        .collect(Collectors.toList());

        return this.paymentRepository.saveAll(payments);
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
    @Transactional
    public List<Payment> createPayShellPayments(String userId, Integer withdrawPercent, MembershipSku sku, ZonedDateTime currentExpireTime) {
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
                                .tradeNum(UUID.randomUUID().toString().replace("-", ""))
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
     * @param wechatOrder   {@link WechatOrder}.
     * @param sku           {@link ShellSku}.
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
     * @param userId    User id.
     * @param pageable  {@link Pageable}.
     * @return {@link Payment}.
     */
    public Page<Payment> retrievePaymentsByUserId(String userId, Pageable pageable) {
        return this.paymentRepository.findAllValidPaymentsByUserId(userId, pageable);
    }

}