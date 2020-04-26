package com.keepreal.madagascar.lemur.controller;

import io.swagger.annotations.ApiParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import swagger.api.IslandApi;
import swagger.model.CheckIslandResponse;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@RestController
public class IslandController implements IslandApi {

    public ResponseEntity<CheckIslandResponse> apiV1IslandsCheckNameGet(@NotNull @ApiParam(value = "name", required = true) @Valid @RequestParam(value = "name", required = true) String name) {
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }
}
