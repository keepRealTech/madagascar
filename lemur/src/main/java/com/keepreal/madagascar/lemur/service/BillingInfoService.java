package com.keepreal.madagascar.lemur.service;

import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.vanga.BillingInfoMessage;
import com.keepreal.madagascar.vanga.BillingInfoResponse;
import com.keepreal.madagascar.vanga.BillingInfoServiceGrpc;
import com.keepreal.madagascar.vanga.RetrieveBillingInfoByUserIdRequest;
import com.keepreal.madagascar.vanga.UpdateBillingInfoByUserIdRequest;
import io.grpc.Channel;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Represents the billing info service.
 */
@Service
@Slf4j
public class BillingInfoService {

    private final Channel channel;

    /**
     * Constructs the billing info service.
     *
     * @param channel GRpc managed channel connection to service Vanga.
     */
    public BillingInfoService(@Qualifier("vangaChannel") Channel channel) {
        this.channel = channel;
    }

    /**
     * Retrieves the billing info by user id.
     *
     * @param userId User id.
     * @return {@link BillingInfoMessage}.
     */
    public BillingInfoMessage retrieveBillingInfoByUserId(String userId) {
        BillingInfoServiceGrpc.BillingInfoServiceBlockingStub stub = BillingInfoServiceGrpc.newBlockingStub(this.channel);

        RetrieveBillingInfoByUserIdRequest request = RetrieveBillingInfoByUserIdRequest.newBuilder()
                .setUserId(userId)
                .build();

        BillingInfoResponse billingInfoResponse;
        try {
            billingInfoResponse = stub.retrieveBillingInfoByUserId(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(billingInfoResponse)
                || !billingInfoResponse.hasStatus()) {
            log.error(Objects.isNull(billingInfoResponse) ? "Retrieve billing info returned null." : billingInfoResponse.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != billingInfoResponse.getStatus().getRtn()) {
            throw new KeepRealBusinessException(billingInfoResponse.getStatus());
        }

        return billingInfoResponse.getBillingInfo();
    }

    /**
     * Updates billing info by id.
     *
     * @param userId        User id.
     * @param name          User real name.
     * @param accountNumber Account number.
     * @param idNumber      Id number.
     * @param mobile        Mobile.
     * @return {@link BillingInfoMessage}.
     */
    public BillingInfoMessage updateBillingInfoByUserId(String userId, String name, String accountNumber,
                                                        String idNumber, String mobile) {
        BillingInfoServiceGrpc.BillingInfoServiceBlockingStub stub = BillingInfoServiceGrpc.newBlockingStub(this.channel);

        UpdateBillingInfoByUserIdRequest request = UpdateBillingInfoByUserIdRequest.newBuilder()
                .setUserId(userId)
                .setName(name)
                .setAccountNumber(accountNumber)
                .setMobile(mobile)
                .setIdNumber(idNumber)
                .build();

        BillingInfoResponse billingInfoResponse;
        try {
            billingInfoResponse = stub.updateBillingInfoById(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(billingInfoResponse)
                || !billingInfoResponse.hasStatus()) {
            log.error(Objects.isNull(billingInfoResponse) ? "Update billing info returned null." : billingInfoResponse.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != billingInfoResponse.getStatus().getRtn()) {
            throw new KeepRealBusinessException(billingInfoResponse.getStatus());
        }

        return billingInfoResponse.getBillingInfo();
    }

}