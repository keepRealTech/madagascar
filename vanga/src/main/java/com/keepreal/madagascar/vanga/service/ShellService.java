package com.keepreal.madagascar.vanga.service;

import com.keepreal.madagascar.vanga.model.Balance;
import com.keepreal.madagascar.vanga.model.ShellSku;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

/**
 * Represents the shell service.
 */
@Service
public class ShellService {

    private final BalanceService balanceService;
    private final PaymentService paymentService;
    private final IOSOrderService iosOrderService;

    /**
     * Constructs the shell service.
     *
     * @param balanceService  {@link BalanceService}.
     * @param paymentService  {@link PaymentService}.
     * @param iosOrderService {@link IOSOrderService}.
     */
    public ShellService(BalanceService balanceService,
                        PaymentService paymentService,
                        IOSOrderService iosOrderService) {
        this.balanceService = balanceService;
        this.paymentService = paymentService;
        this.iosOrderService = iosOrderService;
    }

    /**
     * Verifies the ios receipt and buy shell for user.
     *
     * @param userId  User id.
     * @param receipt Receipt content.
     * @param sku     {@link ShellSku}.
     * @return {@link Balance}.
     */
    @Transactional
    public Balance buyShell(String userId, String receipt, ShellSku sku) {
        Balance userBalance = this.balanceService.retrieveOrCreateBalanceIfNotExistsByUserId(userId);

        String transactionId = this.iosOrderService.verify(receipt, sku);
        this.paymentService.createBuyShellPayments(userId, sku, transactionId);
        return this.balanceService.addOnShells(userBalance, sku.getShells());
    }

}
