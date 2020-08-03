package com.keepreal.madagascar.baobob.service;

import com.keepreal.madagascar.baobob.CheckOffiAccountLoginRequest;
import com.keepreal.madagascar.baobob.CheckSignatureRequest;
import com.keepreal.madagascar.baobob.CheckSignatureResponse;
import com.keepreal.madagascar.baobob.GenerateQrcodeResponse;
import com.keepreal.madagascar.baobob.HandleEventRequest;
import com.keepreal.madagascar.baobob.LoginRequest;
import com.keepreal.madagascar.baobob.LoginResponse;
import com.keepreal.madagascar.baobob.NullRequest;
import com.keepreal.madagascar.baobob.NullResponse;
import com.keepreal.madagascar.baobob.ReactorLoginServiceGrpc;
import com.keepreal.madagascar.baobob.loginExecutor.DefaultLoginExecutorSelectorImpl;
import com.keepreal.madagascar.baobob.loginExecutor.LoginExecutorSelector;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;

/**
 * Represents the login grpc service.
 */
@GRpcService
public class LoginService extends ReactorLoginServiceGrpc.LoginServiceImplBase {

    private final WechatOffiAccountService wechatOffiAccountService;
    private final LoginExecutorSelector loginExecutorSelector;

    /**
     * Constructs the login service.
     *
     * @param defaultLoginExecutorSelector {@link DefaultLoginExecutorSelectorImpl}.
     */
    public LoginService(DefaultLoginExecutorSelectorImpl defaultLoginExecutorSelector,
                        WechatOffiAccountService wechatOffiAccountService) {
        this.loginExecutorSelector = defaultLoginExecutorSelector;
        this.wechatOffiAccountService = wechatOffiAccountService;
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

    @Override
    public Mono<GenerateQrcodeResponse> generateQrcode(Mono<NullRequest> request) {
        GenerateQrcodeResponse tempQrcode = this.wechatOffiAccountService.getTempQrcode();
        return Mono.just(tempQrcode);
    }

    @Override
    public Mono<CheckSignatureResponse> checkSignature(Mono<CheckSignatureRequest> checkSignatureRequest) {
        return checkSignatureRequest.flatMap(request ->
                Mono.just(this.wechatOffiAccountService.checkSignature(request)));
    }

    @Override
    public Mono<NullResponse> handleEvent(Mono<HandleEventRequest> handleEventRequest) {
        return handleEventRequest.flatMap(request ->
                Mono.just(this.wechatOffiAccountService.handleEvent(request)));
    }

    @Override
    public Mono<LoginResponse> checkOffiAccountLogin(Mono<CheckOffiAccountLoginRequest> checkOffiAccountLoginRequest) {
        return checkOffiAccountLoginRequest.flatMap(this.wechatOffiAccountService::checkOffiAccountLogin);
    }
}
