package com.keepreal.madagascar.lemur.controller;

import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.lemur.config.IOSClientConfiguration;
import com.keepreal.madagascar.lemur.dtoFactory.BalanceDTOFactory;
import com.keepreal.madagascar.lemur.dtoFactory.BillingInfoDTOFactory;
import com.keepreal.madagascar.lemur.service.BalanceService;
import com.keepreal.madagascar.lemur.service.BillingInfoService;
import com.keepreal.madagascar.lemur.service.PaymentService;
import com.keepreal.madagascar.lemur.util.HttpContextUtils;
import com.keepreal.madagascar.vanga.BalanceMessage;
import com.keepreal.madagascar.vanga.BillingInfoMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import swagger.api.BillingApi;
import swagger.model.BalanceResponse;
import swagger.model.BillingInfoResponse;
import swagger.model.BillingInfoResponseV11;
import swagger.model.ConfigurationDTO;
import swagger.model.H5RedirectDTO;
import swagger.model.H5RedirectResponse;
import swagger.model.PostWithdrawRequest;
import swagger.model.PutBillingInfoRequest;
import swagger.model.PutBillingInfoRequestV11;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents the billing controller.
 */
@RestController
public class BillingController implements BillingApi {

    private final BillingInfoService billingInfoService;
    private final BillingInfoDTOFactory billingInfoDTOFactory;
    private final Map<Integer, ConfigurationDTO> iOSConfigVersionMap = new HashMap<>();
    private final BalanceService balanceService;
    private final BalanceDTOFactory balanceDTOFactory;
    private final PaymentService paymentService;

    /**
     * Constructs the billing controller.
     *
     * @param billingInfoService     {@link BillingInfoService}.
     * @param billingInfoDTOFactory  {@link BillingInfoDTOFactory}.
     * @param iosClientConfiguration {@link IOSClientConfiguration}.
     * @param balanceService         {@link BalanceService}.
     * @param balanceDTOFactory      {@link BalanceDTOFactory}.
     * @param paymentService         {@link PaymentService}.
     */
    public BillingController(BillingInfoService billingInfoService,
                             BillingInfoDTOFactory billingInfoDTOFactory,
                             IOSClientConfiguration iosClientConfiguration,
                             BalanceService balanceService,
                             BalanceDTOFactory balanceDTOFactory,
                             PaymentService paymentService) {
        this.billingInfoService = billingInfoService;
        this.billingInfoDTOFactory = billingInfoDTOFactory;
        this.balanceService = balanceService;
        this.balanceDTOFactory = balanceDTOFactory;
        this.paymentService = paymentService;
        this.iOSConfigVersionMap.putAll(iosClientConfiguration.getVersionInfoMap());
    }

    /**
     * Implements the balance get api.
     *
     * @return {@link BalanceResponse}.
     */
    @CrossOrigin
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
     * Implements the billing info get api.
     *
     * @return {@link BillingInfoResponseV11}.
     */
    @CrossOrigin
    @Override
    public ResponseEntity<BillingInfoResponseV11> apiV11BillingInfoGet() {
        String userId = HttpContextUtils.getUserIdFromContext();
        BillingInfoMessage billingInfoMessage = this.billingInfoService.retrieveBillingInfoByUserId(userId);

        BillingInfoResponseV11 response = new BillingInfoResponseV11();
        response.setData(this.billingInfoDTOFactory.v11ValueOf(billingInfoMessage));
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
                userId,
                putBillingInfoRequest.getName(),
                putBillingInfoRequest.getAccountNumber(),
                putBillingInfoRequest.getIdentityNumber(),
                putBillingInfoRequest.getMobile(),
                putBillingInfoRequest.getIdFrontUrl(),
                putBillingInfoRequest.getIdBackUrl());

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
    @CrossOrigin
    @Override
    public ResponseEntity<BillingInfoResponseV11> apiV11BillingInfoPut(PutBillingInfoRequestV11 putBillingInfoRequest) {
        String userId = HttpContextUtils.getUserIdFromContext();

        BillingInfoMessage billingInfoMessage = this.billingInfoService.updateBillingInfoByUserIdV2(
                userId,
                putBillingInfoRequest.getName(),
                putBillingInfoRequest.getMobile(),
                putBillingInfoRequest.getAlipayAccount());

        BillingInfoResponseV11 response = new BillingInfoResponseV11();
        response.setData(this.billingInfoDTOFactory.v11ValueOf(billingInfoMessage));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the create withdraw request api.
     *
     * @param postWithdrawRequest (required) {@link PostWithdrawRequest}.
     * @return {@link BalanceResponse}.
     */
    @CrossOrigin
    @Override
    public ResponseEntity<BalanceResponse> apiV1BalancesWithdrawPost(PostWithdrawRequest postWithdrawRequest) {
        String userId = HttpContextUtils.getUserIdFromContext();

        BalanceMessage balanceMessage = this.paymentService.submitWithdrawRequest(userId, postWithdrawRequest.getAmountInCents());

        BalanceResponse response = new BalanceResponse();
        response.setData(this.balanceDTOFactory.valueOf(balanceMessage));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the ios get wallet h5 url.
     *
     * @param version (required) Version.
     * @return {@link H5RedirectResponse}.
     */
    @CrossOrigin
    @Override
    public ResponseEntity<H5RedirectResponse> apiV1BalancesMyWalletIosGet(Integer version) {
        String userId = HttpContextUtils.getUserIdFromContext();
        ConfigurationDTO configurationDTO = this.iOSConfigVersionMap.get(version);

        H5RedirectDTO data = new H5RedirectDTO();
        if (PaymentController.AUDIT_USER_IDS.contains(userId) || Objects.isNull(configurationDTO) || configurationDTO.getAudit()) {
            data.setUrl("http://tt.keepreal.cn/my/wallet/notsupport");
        } else {
            data.setUrl("http://tt.keepreal.cn/my/wallet");
        }

        H5RedirectResponse response = new H5RedirectResponse();
        response.setData(data);
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
