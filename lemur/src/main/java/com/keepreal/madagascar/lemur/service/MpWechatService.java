package com.keepreal.madagascar.lemur.service;

import com.keepreal.madagascar.common.EmptyMessage;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.hawksbill.MpWechatServiceGrpc;
import com.keepreal.madagascar.hawksbill.RetrievePermanentQRCodeResponse;
import io.grpc.Channel;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Represents the follow service.
 */
@Service
@Slf4j
public class MpWechatService {

    private final Channel channel;

    /**
     * Constructs the mp wechat service.
     *
     * @param channel GRpc managed channel connection to service Hawksbill.
     */
    public MpWechatService(@Qualifier("hawksbillChannel") Channel channel) {
        this.channel = channel;
    }

    /**
     * 获取永久二维码
     *
     * @return 二维码ticket
     */
    public String retrievePermanentQRCode() {
        MpWechatServiceGrpc.MpWechatServiceBlockingStub stub = MpWechatServiceGrpc.newBlockingStub(this.channel);
        RetrievePermanentQRCodeResponse response;
        try {
            response = stub.retrievePermanentQRCode(EmptyMessage.newBuilder().build());
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(response)
                || !response.hasStatus()) {
            log.error(Objects.isNull(response) ? "retrieve Permanent QRCode returned null." : response.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getStatus().getRtn()) {
            throw new KeepRealBusinessException(response.getStatus());
        }

        return response.getTicket();
    }

}
