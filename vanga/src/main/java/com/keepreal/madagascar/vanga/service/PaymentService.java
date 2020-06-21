package com.keepreal.madagascar.vanga.service;

import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.common.snowflake.generator.LongIdGenerator;
import com.keepreal.madagascar.vanga.model.Balance;
import com.keepreal.madagascar.vanga.model.MembershipSku;
import com.keepreal.madagascar.vanga.model.Payment;
import com.keepreal.madagascar.vanga.model.PaymentState;
import com.keepreal.madagascar.vanga.model.PaymentType;
import com.keepreal.madagascar.vanga.model.WechatOrder;
import com.keepreal.madagascar.vanga.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
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
                                .build())
                        .collect(Collectors.toList());

        return this.paymentRepository.saveAll(payments);
    }

    /**
     * Creates new shell payments.
     *
     * @param userId          User id.
     * @param withdrawPercent Withdraw percent.
     * @param sku             {@link MembershipSku}.
     * @return {@link Payment}.
     */
    @Transactional
    public List<Payment> createNewShellPayments(String userId, Integer withdrawPercent, MembershipSku sku) {
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
                                .build())
                        .collect(Collectors.toList());

        return this.paymentRepository.saveAll(payments);
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

}