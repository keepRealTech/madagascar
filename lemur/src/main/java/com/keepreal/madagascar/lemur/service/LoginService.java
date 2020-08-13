package com.keepreal.madagascar.lemur.service;

import com.keepreal.madagascar.baobob.CheckSignatureRequest;
import com.keepreal.madagascar.baobob.CheckSignatureResponse;
import com.keepreal.madagascar.baobob.GenerateQrcodeResponse;
import com.keepreal.madagascar.baobob.HandleEventRequest;
import com.keepreal.madagascar.baobob.LoginRequest;
import com.keepreal.madagascar.baobob.LoginResponse;
import com.keepreal.madagascar.baobob.LoginServiceGrpc;
import com.keepreal.madagascar.baobob.MpSceneLoginPayload;
import com.keepreal.madagascar.common.EmptyMessage;
import com.keepreal.madagascar.common.LoginType;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import io.grpc.Channel;
import io.grpc.StatusRuntimeException;
import io.opentracing.Tracer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Represents the login service.
 */
@Service
@Slf4j
public class LoginService {

    private final Channel channel;

    /**
     * Constructs the login service.
     *
     * @param channel GRpc managed channel connection to service Baobob.
     */
    public LoginService(@Qualifier("baobobChannel") Channel channel, Tracer tracer) {
        this.channel = channel;
    }

    /**
     * Logs in.
     *
     * @param request {@link LoginRequest}.
     * @return {@link LoginResponse}.
     */
    public LoginResponse login(LoginRequest request) {
        LoginServiceGrpc.LoginServiceBlockingStub stub =
                LoginServiceGrpc.newBlockingStub(this.channel)
                        .withDeadlineAfter(10, TimeUnit.SECONDS);

        LoginResponse loginResponse;
        try {
            loginResponse = stub.login(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(loginResponse)
                || !loginResponse.hasStatus()) {
            log.error(Objects.isNull(loginResponse) ? "GRpc login returned null." : loginResponse.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_GRPC_LOGIN_INVALID);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != loginResponse.getStatus().getRtn()) {
            throw new KeepRealBusinessException(loginResponse.getStatus());
        }

        return loginResponse;
    }

    /**
     * Checks wechat mp signature.
     *
     * @param request {@link CheckSignatureRequest}.
     * @return True if valid.
     */
    public Boolean checkSignature(CheckSignatureRequest request) {
        LoginServiceGrpc.LoginServiceBlockingStub stub = LoginServiceGrpc.newBlockingStub(this.channel);
        CheckSignatureResponse response;

        try {
            response = stub.checkSignature(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(response) || !response.hasStatus()) {
            log.error(Objects.isNull(response) ? "GRpc login returned null." : response.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getStatus().getRtn()) {
            throw new KeepRealBusinessException(response.getStatus());
        }

        return true;
    }

    /**
     * Generates the wechat mp qr code ticket.
     *
     * @return {@link GenerateQrcodeResponse}.
     */
    public GenerateQrcodeResponse generateQrcode() {
        LoginServiceGrpc.LoginServiceBlockingStub stub = LoginServiceGrpc.newBlockingStub(this.channel);
        GenerateQrcodeResponse generateQrcodeResponse;

        try {
            generateQrcodeResponse = stub.generateQrcode(EmptyMessage.newBuilder().build());
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(generateQrcodeResponse)
                || !generateQrcodeResponse.hasStatus()) {
            log.error(Objects.isNull(generateQrcodeResponse) ? "GRpc login returned null." : generateQrcodeResponse.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != generateQrcodeResponse.getStatus().getRtn()) {
            throw new KeepRealBusinessException(generateQrcodeResponse.getStatus());
        }
        return generateQrcodeResponse;
    }

    /**
     * Handles the wechat media platform events.
     *
     * @param fromUserName User name.
     * @param event        Event type.
     * @param eventKey     Event key.
     */
    public void handleEvent(String fromUserName, String event, String eventKey) {
        LoginServiceGrpc.LoginServiceBlockingStub stub = LoginServiceGrpc.newBlockingStub(this.channel);
        HandleEventRequest request = HandleEventRequest.newBuilder().setOpedId(fromUserName)
                .setEventKey(eventKey).setEvent(event).build();
        try {
            stub.handleEvent(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }
    }

    /**
     * Checks the wechat mp login status.
     *
     * @param sceneId Scene id.
     * @return {@link LoginResponse}.
     */
    public LoginResponse checkWechatMpAccountLogin(String sceneId) {
        LoginServiceGrpc.LoginServiceBlockingStub stub = LoginServiceGrpc.newBlockingStub(this.channel);

        LoginRequest loginRequest = LoginRequest.newBuilder()
                .setMpScenePayload(MpSceneLoginPayload.newBuilder()
                        .setSceneId(sceneId)
                        .build())
                .setLoginType(LoginType.LOGIN_WEB_MP_WECHAT)
                .build();

        LoginResponse loginResponse;
        try {
            loginResponse = stub.login(loginRequest);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != loginResponse.getStatus().getRtn()) {
            throw new KeepRealBusinessException(loginResponse.getStatus());
        }

        return loginResponse;
    }

}
