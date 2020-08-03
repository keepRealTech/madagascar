package com.keepreal.madagascar.lemur.service;

import com.keepreal.madagascar.baobob.CheckOffiAccountLoginRequest;
import com.keepreal.madagascar.baobob.CheckSignatureRequest;
import com.keepreal.madagascar.baobob.CheckSignatureResponse;
import com.keepreal.madagascar.baobob.GenerateQrcodeResponse;
import com.keepreal.madagascar.baobob.HandleEventRequest;
import com.keepreal.madagascar.baobob.LoginRequest;
import com.keepreal.madagascar.baobob.LoginResponse;
import com.keepreal.madagascar.baobob.LoginServiceGrpc;
import com.keepreal.madagascar.baobob.NullRequest;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import io.grpc.Channel;
import io.grpc.StatusRuntimeException;
import io.opentracing.Tracer;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public LoginService(@Qualifier("baobobChannel") Channel channel,
                        Tracer tracer) {
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

    public GenerateQrcodeResponse generateQrcode() {
        LoginServiceGrpc.LoginServiceBlockingStub stub = LoginServiceGrpc.newBlockingStub(this.channel);
        GenerateQrcodeResponse generateQrcodeResponse;

        try {
            generateQrcodeResponse = stub.generateQrcode(NullRequest.newBuilder().build());
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

    public Map<String, String> parseXml(HttpServletRequest request) {
        Map<String, String> map = new HashMap<String, String>();
        InputStream inputStream = null;

        try {
            inputStream = request.getInputStream();
        } catch (IOException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }
        SAXReader reader = new SAXReader();
        Document document = null;
        try {
            document = reader.read(inputStream);
        } catch (DocumentException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        Element root = document.getRootElement();
        List<Element> elementList = root.elements();
        for (Element e : elementList){
            map.put(e.getName(), e.getText());
        }

        try {
            inputStream.close();
        } catch (IOException e) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }
        return map;
    }

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

    public LoginResponse checkOffiAccountLogin(String sceneId) {
        LoginServiceGrpc.LoginServiceBlockingStub stub = LoginServiceGrpc.newBlockingStub(this.channel);
        CheckOffiAccountLoginRequest request = CheckOffiAccountLoginRequest.newBuilder().setSceneId(sceneId).build();
        LoginResponse loginResponse;

        try {
            loginResponse = stub.checkOffiAccountLogin(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != loginResponse.getStatus().getRtn()) {
            throw new KeepRealBusinessException(loginResponse.getStatus());
        }

        return loginResponse;
    }
}
