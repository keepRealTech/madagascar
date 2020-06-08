package com.keepreal.madagascar.lemur.controller;

import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.lemur.dtoFactory.BalanceDTOFactory;
import com.keepreal.madagascar.lemur.dtoFactory.BillingInfoDTOFactory;
import com.keepreal.madagascar.lemur.service.BalanceService;
import com.keepreal.madagascar.lemur.service.BillingInfoService;
import com.keepreal.madagascar.lemur.service.PaymentService;
import com.keepreal.madagascar.lemur.util.HttpContextUtils;
import com.keepreal.madagascar.vanga.BalanceMessage;
import com.keepreal.madagascar.vanga.BillingInfoMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestController;
import swagger.api.ApiUtil;
import swagger.api.BillingApi;
import swagger.model.BalanceResponse;
import swagger.model.BillingInfoResponse;
import swagger.model.PostWithDrawRequest;
import swagger.model.PutBillingInfoRequest;

/**
 * Represents the billing controller.
 */
@RestController
public class BillingController implements BillingApi {

    private final BillingInfoService billingInfoService;
    private final BillingInfoDTOFactory billingInfoDTOFactory;
    private final BalanceService balanceService;
    private final BalanceDTOFactory balanceDTOFactory;
    private final PaymentService paymentService;

    /**
     * Constructs the billing controller.
     *
     * @param billingInfoService    {@link BillingInfoService}.
     * @param billingInfoDTOFactory {@link BillingInfoDTOFactory}.
     * @param balanceService        {@link BalanceService}.
     * @param balanceDTOFactory     {@link BalanceDTOFactory}.
     * @param paymentService        {@link PaymentService}.
     */
    public BillingController(BillingInfoService billingInfoService,
                             BillingInfoDTOFactory billingInfoDTOFactory,
                             BalanceService balanceService,
                             BalanceDTOFactory balanceDTOFactory,
                             PaymentService paymentService) {
        this.billingInfoService = billingInfoService;
        this.billingInfoDTOFactory = billingInfoDTOFactory;
        this.balanceService = balanceService;
        this.balanceDTOFactory = balanceDTOFactory;
        this.paymentService = paymentService;
    }

    /**
     * Implements the balance get api.
     *
     * @return {@link BalanceResponse}.
     */
    @Override
    public ResponseEntity<BalanceResponse> apiV1BalancesMyWalletGet() {
        String userId = HttpContextUtils.getUserIdFromContext();
        BalanceMessage balanceMessage = this.balanceService.retrieveBalanceByUserId(userId);

        BalanceResponse response = new BalanceResponse();
        response.setData(this.balanceDTOFactory.valueOf(balanceMessage));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the billing info get api.
     *
     * @return {@link BillingInfoResponse}.
     */
    @Override
    public ResponseEntity<BillingInfoResponse> apiV1BillingInfoGet() {
        String userId = HttpContextUtils.getUserIdFromContext();
        BillingInfoMessage billingInfoMessage = this.billingInfoService.retrieveBillingInfoByUserId(userId);

        BillingInfoResponse response = new BillingInfoResponse();
        response.setData(this.billingInfoDTOFactory.valueOf(billingInfoMessage));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the billing info put api.
     *
     * @return {@link BillingInfoResponse}.
     */
    @Override
    public ResponseEntity<BillingInfoResponse> apiV1BillingInfoPut(PutBillingInfoRequest putBillingInfoRequest) {
        String userId = HttpContextUtils.getUserIdFromContext();

        BillingInfoMessage billingInfoMessage = this.billingInfoService.updateBillingInfoByUserId(
                userId, putBillingInfoRequest.getName(), putBillingInfoRequest.getAccountNumber(),
                putBillingInfoRequest.getIdentityNumber(), putBillingInfoRequest.getMobile());

        BillingInfoResponse response = new BillingInfoResponse();
        response.setData(this.billingInfoDTOFactory.valueOf(billingInfoMessage));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the create withdraw request api.
     *
     * @param postWithDrawRequest  (required) {@link PostWithDrawRequest}.
     * @return {@link BalanceResponse}.
     */
    @Override
    public ResponseEntity<BalanceResponse> apiV1BalancesWithDrawPost(PostWithDrawRequest postWithDrawRequest) {

    }

}
