package com.keepreal.madagascar.lemur.controller;

import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.lemur.dtoFactory.SkuDTOFactory;
import com.keepreal.madagascar.lemur.service.SkuService;
import com.keepreal.madagascar.vanga.MembershipSkuMessage;
import com.keepreal.madagascar.vanga.ShellSkuMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import swagger.api.SkuApi;
import swagger.model.IOSShellSkusResponse;
import swagger.model.MembershipSkusResponse;
import swagger.model.SponsorSkusResponse;
import swagger.model.SupportSkusResponse;
import swagger.model.WechatShellSkusResponse;

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
     * Implements the ios shell skus get api.
     *
     * @return {@link IOSShellSkusResponse}.
     */
    @Override
    public ResponseEntity<IOSShellSkusResponse> apiV1BalancesSkusGet() {
        List<ShellSkuMessage> shellSkuMessageList = this.skuService.retrieveShellSkus(false);

        IOSShellSkusResponse response = new IOSShellSkusResponse();
        response.setData(shellSkuMessageList.stream()
                .map(this.skuDTOFactory::iosValueOf)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the wechat shell skus get api.
     *
     * @return {@link WechatShellSkusResponse}.
     */
    @Override
    public ResponseEntity<WechatShellSkusResponse> apiV1BalancesWechatSkusGet() {
        List<ShellSkuMessage> shellSkuMessageList = this.skuService.retrieveShellSkus(true);

        WechatShellSkusResponse response = new WechatShellSkusResponse();
        response.setData(shellSkuMessageList.stream()
                .map(this.skuDTOFactory::wechatValueOf)
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
    public ResponseEntity<MembershipSkusResponse> apiV1MembershipIdSkusGet(String id, Boolean permanent) {
        List<MembershipSkuMessage> shellSkuMessageList = this.skuService.retrieveMembershipSkusByMembershipIds(id);

        if (!permanent) {
            if (shellSkuMessageList.size() == 1 && shellSkuMessageList.get(0).getPermanent()) {
                throw new KeepRealBusinessException(ErrorCode.REQUEST_PERMANENT_VERSION_LOW_ERROR);
            }
        }

        MembershipSkusResponse response = new MembershipSkusResponse();
        response.setData(shellSkuMessageList.stream()
                .map(this.skuDTOFactory::valueOf)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements island support skus get api.
     *
     * @param id id (required) Island id.
     * @return {@link SupportSkusResponse}.
     */
    @Override
    public ResponseEntity<SupportSkusResponse> apiV1IslandsIdSupportSkusGet(String id) {
        SupportSkusResponse response = new SupportSkusResponse();
        response.setData(this.skuDTOFactory.valueOf(this.skuService.retrieveSupportSkus()));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements sponsor skus get api.
     *
     * @param id id (required) island id
     * @return {@link SponsorSkusResponse}
     */
    @Override
    public ResponseEntity<SponsorSkusResponse> apiV2IslandsIdSponsorsSkusGet(String id) {

        com.keepreal.madagascar.vanga.SponsorSkusResponse responseMessage = this.skuService.retrieveSponsorSkus(id);

        SponsorSkusResponse response = new SponsorSkusResponse();
        response.setData(this.skuDTOFactory.sponsorSkusValueOf(id, responseMessage.getSponsorSkusList(), responseMessage.getCount()));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
