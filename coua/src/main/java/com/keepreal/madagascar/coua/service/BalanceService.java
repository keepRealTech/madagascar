package com.keepreal.madagascar.coua.service;

import com.keepreal.madagascar.common.CommonStatus;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.vanga.BalanceServiceGrpc;
import com.keepreal.madagascar.vanga.CreateBalanceByUserIdRequest;
import io.grpc.Channel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Represents the balance service.
 */
@Service
public class BalanceService {

    private final Channel channel;

    /**
     * Constructor the balance service.
     *
     * @param channel GRpc managed channel connection to service Vanga.
     */
    public BalanceService(@Qualifier("vangaChannel") Channel channel) {
        this.channel = channel;
    }

    /**
     * Creates balance by user id.
     *
     * @param userId User id.
     */
    public void createBalanceByUserId(String userId) {
        BalanceServiceGrpc.BalanceServiceBlockingStub stub = BalanceServiceGrpc.newBlockingStub(this.channel);

        CreateBalanceByUserIdRequest request = CreateBalanceByUserIdRequest.newBuilder()
                .setUserId(userId)
                .build();

        CommonStatus response;
        try {
            response = stub.createBalanceByUserId(request);
        } catch (Exception e) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (Objects.isNull(response)) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getRtn()) {
            throw new KeepRealBusinessException(response);
        }
    }

}
