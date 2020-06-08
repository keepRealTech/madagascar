package com.keepreal.madagascar.vanga.service;

import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.vanga.model.Balance;
import com.keepreal.madagascar.vanga.model.Payment;
import com.keepreal.madagascar.vanga.model.PaymentType;
import com.keepreal.madagascar.vanga.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Represents the payment service.
 */
@Service
public class PaymentService {

    private final BalanceService balanceService;
    private final PaymentRepository paymentRepository;

    public PaymentService(BalanceService balanceService, PaymentRepository paymentRepository) {
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

        if (balance.getBalanceEligibleInCents() < amountInCents) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_USER_BALANCE_WITHDRAW_LIMIT_ERROR);
        }

        ZonedDateTime todayStart = ZonedDateTime.now().toLocalDate().atStartOfDay(ZoneId.systemDefault());

        List<Payment> payments = this.paymentRepository.findAllByTypeAndCreatedTimeAfterAndDeletedIsFalse(
                PaymentType.WITHDRAW.getValue(), todayStart.toInstant().toEpochMilli());

        Long current = payments.stream().map(Payment::getAmountInCents).reduce(0L, Long::sum);
        if (current + amountInCents > balance.getWithdrawDayLimitInCents()) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_USER_BALANCE_WITHDRAW_DAY_LIMIT_ERROR);
        }

        balance.setBalanceEligibleInCents(balance.getBalanceEligibleInCents() - amountInCents);
        balance.setBalanceInCents(balance.getBalanceInCents() - amountInCents);
        Payment payment = Payment.builder()
                .userId(userId)
                .amountInCents(amountInCents)
                .type(PaymentType.WITHDRAW.getValue())
                .build();
        balance = this.balanceService.updateBalance(balance);
        this.paymentRepository.save(payment);

        return balance;
    }

}
