package com.keepreal.madagascar.lemur.service;

import com.keepreal.madagascar.baobob.LoginRequest;
import com.keepreal.madagascar.baobob.LoginResponse;
import com.keepreal.madagascar.baobob.LoginServiceGrpc;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
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

    private final ManagedChannel managedChannel;

    /**
     * Constructs the login service.
     *
     * @param managedChannel GRpc managed channel connection to service Baobob.
     */
    public LoginService(@Qualifier("baobobChannel") ManagedChannel managedChannel) {
        this.managedChannel = managedChannel;
    }

    /**
     * Logs in.
     *
     * @param request {@link LoginRequest}.
     * @return {@link LoginResponse}.
     */
    public LoginResponse login(LoginRequest request) {
        LoginServiceGrpc.LoginServiceBlockingStub stub =
                LoginServiceGrpc.newBlockingStub(this.managedChannel)
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

}
