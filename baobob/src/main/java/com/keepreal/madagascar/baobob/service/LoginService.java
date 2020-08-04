package com.keepreal.madagascar.baobob.service;

import com.keepreal.madagascar.baobob.CheckSignatureRequest;
import com.keepreal.madagascar.baobob.CheckSignatureResponse;
import com.keepreal.madagascar.baobob.GenerateQrcodeResponse;
import com.keepreal.madagascar.baobob.HandleEventRequest;
import com.keepreal.madagascar.baobob.LoginRequest;
import com.keepreal.madagascar.baobob.LoginResponse;
import com.keepreal.madagascar.baobob.ReactorLoginServiceGrpc;
import com.keepreal.madagascar.baobob.loginExecutor.DefaultLoginExecutorSelectorImpl;
import com.keepreal.madagascar.baobob.loginExecutor.LoginExecutorSelector;
import com.keepreal.madagascar.common.EmptyMessage;
import org.lognet.springboot.grpc.GRpcService;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Represents the login grpc service.
 */
@GRpcService
public class LoginService extends ReactorLoginServiceGrpc.LoginServiceImplBase {

    private final MpWechatService mpWechatService;
    private final LoginExecutorSelector loginExecutorSelector;

    /**
     * Constructs the login service.
     *
     * @param defaultLoginExecutorSelector {@link DefaultLoginExecutorSelectorImpl}.
     */
    public LoginService(DefaultLoginExecutorSelectorImpl defaultLoginExecutorSelector,
                        MpWechatService mpWechatService) {
        this.loginExecutorSelector = defaultLoginExecutorSelector;
        this.mpWechatService = mpWechatService;
    }

    /**
     * Logs in with proper login type.
     *
     * @param loginRequest Login request {@link LoginRequest}.
     * @return {@link LoginResponse}.
     */
    @Override
    public Mono<LoginResponse> login(Mono<LoginRequest> loginRequest) {
        return loginRequest.flatMap(request ->
                this.loginExecutorSelector.select(request.getLoginType()).login(request));
    }

    /**
     * Generates the qrcode ticket.
     *
     * @param emptyRequest {@link EmptyMessage}.
     * @return {@link GenerateQrcodeResponse}.
     */
    @Override
    public Mono<GenerateQrcodeResponse> generateQrcode(Mono<EmptyMessage> emptyRequest) {
        return emptyRequest.flatMap(request -> this.mpWechatService.getTempQrcode());
    }

    /**
     * Checks the signature.
     *
     * @param checkSignatureRequest {@link CheckSignatureRequest}.
     * @return {@link CheckSignatureResponse}.
     */
    @Override
    public Mono<CheckSignatureResponse> checkSignature(Mono<CheckSignatureRequest> checkSignatureRequest) {
        return checkSignatureRequest.map(this.mpWechatService::checkSignature);
    }

    /**
     * Handles the wechat MP events.
     *
     * @param handleEventRequest {@link HandleEventRequest}.
     * @return {@link EmptyMessage}.
     */
    @Override
    public Mono<EmptyMessage> handleEvent(Mono<HandleEventRequest> handleEventRequest) {
        return handleEventRequest.publishOn(Schedulers.elastic())
                .flatMap(request -> Mono.just(this.mpWechatService.handleEvent(request)));
    }

}
