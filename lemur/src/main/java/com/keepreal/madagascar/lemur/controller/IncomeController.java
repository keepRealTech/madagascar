package com.keepreal.madagascar.lemur.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import swagger.api.IncomeApi;
import swagger.model.CurrentMonthSupportResponse;
import swagger.model.IncomeDetailsResponse;
import swagger.model.IncomeMonthlyResponse;
import swagger.model.IncomeProfileResponse;
import swagger.model.IncomeSupportsResponse;
import swagger.model.IncomeType;

/**
 * Represents the income controller.
 */
@RestController
@Slf4j
public class IncomeController implements IncomeApi {

    @Override
    public ResponseEntity<CurrentMonthSupportResponse> apiV1IncomeCurrentMonthGet() {
        return null;
    }

    @Override
    public ResponseEntity<IncomeDetailsResponse> apiV1IncomeDetailByConditionGet(IncomeType type, Long timestamp, String membershipId, Integer page, Integer pageSize) {
        return null;
    }

    @Override
    public ResponseEntity<IncomeMonthlyResponse> apiV1IncomeMonthlyGet() {
        return null;
    }

    @Override
    public ResponseEntity<IncomeProfileResponse> apiV1IncomeProfileGet() {
        return null;
    }

    @Override
    public ResponseEntity<IncomeSupportsResponse> apiV1IncomeSupportListGet(Integer page, Integer pageSize) {
        return null;
    }
}
