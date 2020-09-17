package com.keepreal.madagascar.lemur.service;

import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.vanga.ActivityMessage;
import com.keepreal.madagascar.vanga.RetrieveActivityBonusRequest;
import com.keepreal.madagascar.vanga.RetrieveActivityBonusResponse;
import com.keepreal.madagascar.vanga.SupportActivityGrpc;
import io.grpc.Channel;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@Slf4j
public class SupportActivityService {

    private final Channel channel;

    public SupportActivityService(@Qualifier("vangaChannel") Channel channel) {
        this.channel = channel;
    }

    public ActivityMessage retrieveActivityBonus(String userId) {
        SupportActivityGrpc.SupportActivityBlockingStub stub = SupportActivityGrpc.newBlockingStub(this.channel);

        RetrieveActivityBonusResponse response;

        try {
            response = stub.retrieveActivityBonus(RetrieveActivityBonusRequest.newBuilder()
                    .setUserId(userId)
                    .build());
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(response)
                || !response.hasStatus()) {
            log.error(Objects.isNull(response) ? "Retrieve activity bonus returned null." : response.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getStatus().getRtn()) {
            throw new KeepRealBusinessException(response.getStatus());
        }

        return response.getMessage();
    }
}
