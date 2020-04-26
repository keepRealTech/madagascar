package com.keepreal.madagascar.lemur.service;

import com.keepreal.madagascar.baobob.LoginRequest;
import com.keepreal.madagascar.baobob.LoginResponse;
import com.keepreal.madagascar.baobob.LoginServiceGrpc;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.concurrent.TimeUnit;

/**
 * Represents the login service.
 */
@Service
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

        try {
            return stub.login(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(exception);
        }
    }

    /**
     * Refreshes the token.
     *
     * @param refreshToken Refresh token.
     * @return {@link LoginResponse}.
     */
    public LoginResponse refresh(String refreshToken) {
        throw new NotImplementedException();
    }

}
