package com.keepreal.madagascar.lemur.service;

import com.google.protobuf.StringValue;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.vanga.BillingInfoMessage;
import com.keepreal.madagascar.vanga.BillingInfoResponse;
import com.keepreal.madagascar.vanga.BillingInfoServiceGrpc;
import com.keepreal.madagascar.vanga.RetrieveBillingInfoByUserIdRequest;
import com.keepreal.madagascar.vanga.UpdateBillingInfoByUserIdRequest;
import com.keepreal.madagascar.vanga.UpdateBillingInfoByUserIdRequestV2;
import io.grpc.Channel;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
    public BillingInfoMessage updateBillingInfoByUserId(String userId,
                                                        String name,
                                                        String accountNumber,
                                                        String idNumber,
                                                        String mobile,
                                                        String idFrontUrl,
                                                        String idBackUrl) {
        BillingInfoServiceGrpc.BillingInfoServiceBlockingStub stub = BillingInfoServiceGrpc.newBlockingStub(this.channel);

        UpdateBillingInfoByUserIdRequest.Builder requestBuilder = UpdateBillingInfoByUserIdRequest.newBuilder()
                .setUserId(userId);

        if (!StringUtils.isEmpty(name)) {
            requestBuilder.setName(StringValue.of(name));
        }

        if (!StringUtils.isEmpty(accountNumber)) {
            requestBuilder.setAccountNumber(StringValue.of(accountNumber));
        }

        if (!StringUtils.isEmpty(idNumber)) {
            requestBuilder.setIdNumber(StringValue.of(idNumber));
        }

        if (!StringUtils.isEmpty(mobile)) {
            requestBuilder.setMobile(StringValue.of(mobile));
        }

        if (!StringUtils.isEmpty(idFrontUrl)) {
            requestBuilder.setIdFrontUrl(StringValue.of(idFrontUrl));
        }

        if (!StringUtils.isEmpty(idBackUrl)) {
            requestBuilder.setIdBackUrl(StringValue.of(idBackUrl));
        }

        BillingInfoResponse billingInfoResponse;
        try {
            billingInfoResponse = stub.updateBillingInfoByUserId(requestBuilder.build());
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

    /**
     * Updates billing info by id.
     *
     * @param userId        User id.
     * @param name          User real name.
     * @param mobile        Mobile.
     * @param aliPayAccount Ali pay account.
     * @return {@link BillingInfoMessage}.
     */
    public BillingInfoMessage updateBillingInfoByUserIdV2(String userId,
                                                        String name,
                                                        String mobile,
                                                        String aliPayAccount) {
        BillingInfoServiceGrpc.BillingInfoServiceBlockingStub stub = BillingInfoServiceGrpc.newBlockingStub(this.channel);

        UpdateBillingInfoByUserIdRequestV2.Builder requestBuilder = UpdateBillingInfoByUserIdRequestV2.newBuilder()
                .setUserId(userId);

        if (Objects.nonNull(name)) {
            requestBuilder.setName(StringValue.of(name));
        }

        if (Objects.nonNull(mobile)) {
            requestBuilder.setMobile(StringValue.of(mobile));
        }

        if (Objects.nonNull(aliPayAccount)) {
            requestBuilder.setAliPayAccount(StringValue.of(aliPayAccount));
        }

        BillingInfoResponse billingInfoResponse;
        try {
            billingInfoResponse = stub.updateBillingInfoByUserIdV2(requestBuilder.build());
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