package com.keepreal.madagascar.lemur.controller;

import com.keepreal.madagascar.common.AlipayOrderMessage;
import com.keepreal.madagascar.common.WechatOrderMessage;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.lemur.dtoFactory.BalanceDTOFactory;
import com.keepreal.madagascar.lemur.dtoFactory.OrderDTOFactory;
import com.keepreal.madagascar.lemur.service.OrderService;
import com.keepreal.madagascar.lemur.util.DummyResponseUtils;
import com.keepreal.madagascar.lemur.util.HttpContextUtils;
import com.keepreal.madagascar.vanga.BalanceMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import swagger.api.OrderApi;
import swagger.model.AlipayCheckRequest;
import swagger.model.AlipayOrderResponse;
import swagger.model.BalanceResponse;
import swagger.model.DummyResponse;
import swagger.model.PostIOSMembershipSubscriptionRequest;
import swagger.model.PostIOSOrderRequest;
import swagger.model.PostWechatOrderRequest;
import swagger.model.WechatOrderResponse;

/**
 * Represents the order controller.
 */
@RestController
public class OrderController implements OrderApi {

    private final OrderService orderService;
    private final OrderDTOFactory orderDTOFactory;
    private final BalanceDTOFactory balanceDTOFactory;

    /**
     * Constructs the order controller.
     *
     * @param orderService      {@link OrderService}.
     * @param orderDTOFactory   {@link OrderDTOFactory}.
     * @param balanceDTOFactory {@link BalanceDTOFactory}.
     */
    public OrderController(OrderService orderService,
                           OrderDTOFactory orderDTOFactory,
                           BalanceDTOFactory balanceDTOFactory) {
        this.orderService = orderService;
        this.orderDTOFactory = orderDTOFactory;
        this.balanceDTOFactory = balanceDTOFactory;
    }

    /**
     * Implements the ios shell order post api.
     *
     * @param postIOSOrderRequest (required) {@link PostIOSOrderRequest}.
     * @return {@link BalanceResponse}.
     */
    @Override
    public ResponseEntity<BalanceResponse> apiV1OrdersIosPost(PostIOSOrderRequest postIOSOrderRequest) {
        String userId = HttpContextUtils.getUserIdFromContext();
        BalanceMessage balanceMessage = this.orderService.iosBuyShell(userId,
                postIOSOrderRequest.getShellSkuId(),
                postIOSOrderRequest.getReceipt(),
                postIOSOrderRequest.getTransactionId());

        BalanceResponse response = new BalanceResponse();
        response.setData(this.balanceDTOFactory.valueOf(balanceMessage));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the ios shell order post api.
     *
     * @param postWechatOrderRequest (required) {@link PostWechatOrderRequest}.
     * @return {@link WechatOrderResponse}.
     */
    @Override
    public ResponseEntity<WechatOrderResponse> apiV1OrdersWechatPost(PostWechatOrderRequest postWechatOrderRequest) {
        String userId = HttpContextUtils.getUserIdFromContext();
        WechatOrderMessage wechatOrderMessage = this.orderService.wechatBuyShell(userId,
                postWechatOrderRequest.getOpenId(), postWechatOrderRequest.getShellSkuId());

        WechatOrderResponse response = new WechatOrderResponse();
        response.setData(this.orderDTOFactory.wechatOrderValueOf(wechatOrderMessage));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the wechat order check by order id api.
     *
     * @param id id (required) Order id.
     * @return {@link WechatOrderResponse}.
     */
    @Override
    public ResponseEntity<WechatOrderResponse> apiV1OrdersWechatIdCheckPost(String id) {
        WechatOrderMessage wechatOrderMessage = this.orderService.retrieveWechatOrderById(id);

        WechatOrderResponse response = new WechatOrderResponse();
        response.setData(this.orderDTOFactory.wechatOrderValueOf(wechatOrderMessage));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the alipay order check by order id api.
     *
     * @param id id (required) Order id.
     * @return {@link AlipayOrderResponse}.
     */
    @Override
    public ResponseEntity<AlipayOrderResponse> apiV1OrdersAlipayIdCheckPost(String id,
                                                                            AlipayCheckRequest alipayCheckRequest) {
        AlipayOrderMessage alipayOrder = this.orderService.retrieveAlipayOrderById(id, alipayCheckRequest.getReceipt());

        AlipayOrderResponse response = new AlipayOrderResponse();
        response.setData(this.orderDTOFactory.alipayOrderValueOf(alipayOrder));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the ios pay buy membership api.
     *
     * @param postIOSMembershipSubscriptionRequest (required) {@link PostIOSMembershipSubscriptionRequest}.
     * @return {@link DummyResponse}.
     */
    @Override
    public ResponseEntity<DummyResponse> apiV1OrdersIosMembershipSubscriptionPost(PostIOSMembershipSubscriptionRequest postIOSMembershipSubscriptionRequest) {
        String userId = HttpContextUtils.getUserIdFromContext();
        this.orderService.iosSubscribeMembership(userId,
                postIOSMembershipSubscriptionRequest.getMembershipSkuId(),
                postIOSMembershipSubscriptionRequest.getReceipt(),
                postIOSMembershipSubscriptionRequest.getTransactionId());

        DummyResponse response = new DummyResponse();
        DummyResponseUtils.setRtnAndMessage(response, ErrorCode.REQUEST_SUCC);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
