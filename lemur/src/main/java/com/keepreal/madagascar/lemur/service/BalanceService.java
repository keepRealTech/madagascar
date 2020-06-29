package com.keepreal.madagascar.lemur.service;

import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.vanga.BalanceMessage;
import com.keepreal.madagascar.vanga.BalanceResponse;
import com.keepreal.madagascar.vanga.BalanceServiceGrpc;
import com.keepreal.madagascar.vanga.RetrieveBalanceByUserIdRequest;
import io.grpc.Channel;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Represents the balance service.
 */
@Service
@Slf4j
public class BalanceService {

    private final Channel channel;

    /**
     * Constructs the billing info service.
     *
     * @param channel GRpc managed channel connection to service Vanga.
     */
    public BalanceService(@Qualifier("vangaChannel") Channel channel) {
        this.channel = channel;
    }

    /**
     * Retrieves the billing info by user id.
     *
     * @param userId User id.
     * @return {@link BalanceMessage}.
     */
    public BalanceMessage retrieveBalanceByUserId(String userId) {
        BalanceServiceGrpc.BalanceServiceBlockingStub stub = BalanceServiceGrpc.newBlockingStub(this.channel);

        RetrieveBalanceByUserIdRequest request = RetrieveBalanceByUserIdRequest.newBuilder()
                .setUserId(userId)
                .build();

        BalanceResponse balanceResponse;
        try {
            balanceResponse = stub.retrieveBalanceByUserId(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(balanceResponse)
                || !balanceResponse.hasStatus()) {
            log.error(Objects.isNull(balanceResponse) ? "Retrieve billing info returned null." : balanceResponse.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != balanceResponse.getStatus().getRtn()) {
            throw new KeepRealBusinessException(balanceResponse.getStatus());
        }

        return balanceResponse.getBalance();
    }

}
