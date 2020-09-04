package com.keepreal.madagascar.lemur.controller;

import com.keepreal.madagascar.common.IslandMessage;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.lemur.dtoFactory.SupportDTOFactory;
import com.keepreal.madagascar.lemur.service.IslandService;
import com.keepreal.madagascar.lemur.service.PaymentService;
import com.keepreal.madagascar.vanga.SupportMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import swagger.api.SupportApi;
import swagger.model.SupportsResponse;

@RestController
public class SupportController implements SupportApi {

    private final PaymentService paymentService;
    private final IslandService islandService;
    private final SupportDTOFactory supportDTOFactory;

    public SupportController(PaymentService paymentService,
                             IslandService islandService,
                             SupportDTOFactory supportDTOFactory) {
        this.paymentService = paymentService;
        this.islandService = islandService;
        this.supportDTOFactory = supportDTOFactory;
    }

    @CrossOrigin
    @Override
    public ResponseEntity<SupportsResponse> apiV1IslandsIdSupportGet(String id) {
        IslandMessage islandMessage = this.islandService.retrieveIslandById(id);
        SupportMessage supportMessage = this.paymentService.retrieveSupportInfo(islandMessage.getHostId());

        SupportsResponse response = new SupportsResponse();
        response.setData(this.supportDTOFactory.valueOf(supportMessage, id));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
