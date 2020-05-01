package com.keepreal.madagascar.lemur.config;

import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.lemur.util.DummyResponseUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.error.DefaultWebResponseExceptionTranslator;
import org.springframework.security.oauth2.provider.error.WebResponseExceptionTranslator;
import swagger.model.DummyResponse;

/**
 * Represents a customized oauth exception handler.
 */
public class OAuthExceptionHandler implements WebResponseExceptionTranslator<DummyResponse> {

    /**
     * Overrides the oauth exception translate logic.
     *
     * @param e Exception.
     * @return {@link DummyResponse}.
     * @throws Exception
     */
    @Override
    public ResponseEntity<DummyResponse> translate(Exception e) throws Exception {
        if (!e.getMessage().contains("token expired")) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        DummyResponse response = new DummyResponse();
        DummyResponseUtils.setRtnAndMessage(response, ErrorCode.REQUEST_GRPC_TOKEN_EXPIRED);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
