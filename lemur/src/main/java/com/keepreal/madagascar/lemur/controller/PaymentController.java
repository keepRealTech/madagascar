package com.keepreal.madagascar.lemur.controller;

import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.lemur.dtoFactory.WechatOrderDTOFactory;
import com.keepreal.madagascar.lemur.service.PaymentService;
import com.keepreal.madagascar.lemur.util.DummyResponseUtils;
import com.keepreal.madagascar.lemur.util.HttpContextUtils;
import com.keepreal.madagascar.vanga.WechatOrderMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import swagger.api.PaymentApi;
import swagger.model.DummyResponse;
import swagger.model.SubscribeMemberRequest;
import swagger.model.WechatOrderResponse;

/**
 * Represents the payment controller.
 */
@RestController
public class PaymentController implements PaymentApi {

    private final PaymentService paymentService;
    private final WechatOrderDTOFactory wechatOrderDTOFactory;

    /**
     * Constructs the payment controller.
     *
     * @param paymentService        {@link PaymentService}.
     * @param wechatOrderDTOFactory {@link WechatOrderDTOFactory}.
     */
    public PaymentController(PaymentService paymentService,
                             WechatOrderDTOFactory wechatOrderDTOFactory) {
        this.paymentService = paymentService;
        this.wechatOrderDTOFactory = wechatOrderDTOFactory;
    }

    /**
     * Implements the subscribe with shell balance post api.
     *
     * @param id                     id (required) Islands id.
     * @param subscribeMemberRequest (required) {@link SubscribeMemberRequest}.
     * @return {@link DummyResponse}.
     */
    public ResponseEntity<DummyResponse> apiV1IslandsIdMemberSubscriptionShellPayPost(String id,
                                                                                      SubscribeMemberRequest subscribeMemberRequest) {
        String userId = HttpContextUtils.getUserIdFromContext();

        this.paymentService.subscribeMembershipWithShell(userId, subscribeMemberRequest.getMembershipSkuId());

        DummyResponse response = new DummyResponse();
        DummyResponseUtils.setRtnAndMessage(response, ErrorCode.REQUEST_SUCC);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the subscribe with wechat pay post api.
     *
     * @param id                     id (required) Islands id.
     * @param subscribeMemberRequest (required) {@link SubscribeMemberRequest}.
     * @return {@link WechatOrderResponse}.
     */
    public ResponseEntity<WechatOrderResponse> apiV1IslandsIdMemberSubscriptionWechatPayPost(String id,
                                                                                             SubscribeMemberRequest subscribeMemberRequest) {
        String userId = HttpContextUtils.getUserIdFromContext();

        WechatOrderMessage wechatOrderMessage = this.paymentService.submitSubscribeMembershipWithWechatPay(userId,
                subscribeMemberRequest.getMembershipSkuId());

        WechatOrderResponse response = new WechatOrderResponse();
        response.setData(this.wechatOrderDTOFactory.valueOf(wechatOrderMessage));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
