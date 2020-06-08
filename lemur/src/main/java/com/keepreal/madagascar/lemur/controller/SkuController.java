package com.keepreal.madagascar.lemur.controller;

import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.lemur.dtoFactory.SkuDTOFactory;
import com.keepreal.madagascar.lemur.service.SkuService;
import com.keepreal.madagascar.vanga.MembershipSkuMessage;
import com.keepreal.madagascar.vanga.ShellSkuMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import swagger.api.SkuApi;
import swagger.model.MembershipSkusResponse;
import swagger.model.ShellSkusResponse;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents the sku controller.
 */
@RestController
public class SkuController implements SkuApi {

    private final SkuService skuService;
    private final SkuDTOFactory skuDTOFactory;

    /**
     * Constructs the sku controller.
     *
     * @param skuService    {@link SkuService}.
     * @param skuDTOFactory {@link SkuDTOFactory}.
     */
    public SkuController(SkuService skuService,
                         SkuDTOFactory skuDTOFactory) {
        this.skuService = skuService;
        this.skuDTOFactory = skuDTOFactory;
    }

    /**
     * Implements the shell skus get api.
     *
     * @return {@link ShellSkusResponse}.
     */
    @Override
    public ResponseEntity<ShellSkusResponse> apiV1BalancesSkusGet() {
        List<ShellSkuMessage> shellSkuMessageList = this.skuService.retrieveShellSkus();

        ShellSkusResponse response = new ShellSkusResponse();
        response.setData(shellSkuMessageList.stream()
                .map(this.skuDTOFactory::valueOf)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the membership skus get api.
     *
     * @return {@link MembershipSkusResponse}.
     */
    @Override
    public ResponseEntity<MembershipSkusResponse> apiV1MembershipIdSkusGet(String id) {
        List<MembershipSkuMessage> shellSkuMessageList = this.skuService.retrieveMembershipSkusByMembershipIds(id);

        MembershipSkusResponse response = new MembershipSkusResponse();
        response.setData(shellSkuMessageList.stream()
                .map(this.skuDTOFactory::valueOf)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}