package com.keepreal.madagascar.lemur.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import swagger.api.HealthcheckApi;
import swagger.model.DummyResponse;

/**
 * Represents the healthcheck controller.
 */
@RestController
public class HealthcheckController implements HealthcheckApi {

    /**
     * Implements the health check head api.
     * 
     * @return {@link HttpStatus}.
     */
    @Override
    public ResponseEntity<DummyResponse> apiHealthcheckHead() {
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
