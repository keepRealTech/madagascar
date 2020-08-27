package com.keepreal.madagascar.workflow.settler.service;

import com.keepreal.madagascar.workflow.settler.model.Payment;
import com.keepreal.madagascar.workflow.settler.model.PaymentState;
import com.keepreal.madagascar.workflow.settler.model.PaymentType;
import com.keepreal.madagascar.workflow.settler.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents the payment service.
 */
@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final List<Integer> unsettledType = Arrays.asList(PaymentType.SHELLPAY.getValue(), PaymentType.WECHATPAY.getValue());

    /**
     * Constructs the payment service.
     *
     * @param paymentRepository {@link PaymentRepository}.
     */
    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    /**
     * Retrieves all the unsettled valid payments by today.
     *
     * @return {@link Payment}.
     */
    public List<Payment> retrieveTop5000UnsettledPayments() {
        long today = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        return this.paymentRepository.findTop5000ByTypeInAndStateAndValidAfterBeforeAndDeletedIsFalseOrderByCreatedTime(this.unsettledType,
                PaymentState.OPEN.getValue(), today);
    }

    /**
     * Retrieves all the pending expiring payments by today.
     *
     * @return {@link Payment}.
     */
    public List<Payment> retrieveTop5000ExpiredPendingPayments() {
        long today = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        return this.paymentRepository.findTop5000ByTypeInAndStateAndValidAfterBeforeAndDeletedIsFalseOrderByCreatedTime(this.unsettledType,
                PaymentState.PENDING.getValue(), today);
    }

    /**
     * Counts all open withdraw payments.
     *
     * @return Counts.
     */
    public Long countWithdrawPayments() {
        return this.paymentRepository.countByTypeAndStateAndDeletedIsFalse(PaymentType.WITHDRAW.getValue(), PaymentState.OPEN.getValue());
    }

    /**
     * Updates the state of payments to {@link PaymentState}.
     *
     * @param payments {@link Payment}.
     * @return {@link Payment}.
     */
    @Transactional
    public Long settlePayment(List<Payment> payments) {
        Long totalCents = payments.stream().map(payment -> this.calculateAmount(payment.getAmountInCents(), payment.getWithdrawPercent())).reduce(0L, Long::sum);
        Long totalShells = payments.stream().map(payment -> this.calculateAmount(payment.getAmountInShells(), payment.getWithdrawPercent())).reduce(0L, Long::sum);
        payments = payments.stream().peek(payment -> payment.setState(PaymentState.CLOSED.getValue())).collect(Collectors.toList());
        this.paymentRepository.saveAll(payments);
        return totalCents + totalShells;
    }

    /**
     * Updates the state of payments to {@link PaymentState}.
     *
     * @param payments {@link Payment}.
     * @return {@link Payment}.
     */
    @Transactional
    public Long expiresPayment(List<Payment> payments) {
        Long totalCents = payments.stream().map(payment -> this.calculateAmount(payment.getAmountInCents(), payment.getWithdrawPercent())).reduce(0L, Long::sum);
        payments = payments.stream().peek(payment -> payment.setState(PaymentState.CLOSED.getValue())).collect(Collectors.toList());
        this.paymentRepository.saveAll(payments);
        return totalCents;
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
