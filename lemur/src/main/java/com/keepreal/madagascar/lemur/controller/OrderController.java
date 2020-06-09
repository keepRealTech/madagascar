package com.keepreal.madagascar.lemur.controller;

import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.lemur.dtoFactory.WechatOrderDTOFactory;
import com.keepreal.madagascar.lemur.service.OrderService;
import com.keepreal.madagascar.vanga.WechatOrderMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import swagger.api.OrderApi;
import swagger.model.CheckIOSOrderRequest;
import swagger.model.IOSOrderResponse;
import swagger.model.PostIOSOrderRequest;
import swagger.model.WechatOrderResponse;

/**
 * Represents the order controller.
 */
@RestController
public class OrderController implements OrderApi {

    private final OrderService orderService;
    private final WechatOrderDTOFactory wechatOrderDTOFactory;

    public OrderController(OrderService orderService,
                           WechatOrderDTOFactory wechatOrderDTOFactory) {
        this.orderService = orderService;
        this.wechatOrderDTOFactory = wechatOrderDTOFactory;
    }

    /**
     * Implements the ios shell order post api.
     *
     * @param postIOSOrderRequest (required) {@link PostIOSOrderRequest}.
     * @return {@link IOSOrderResponse}.
     */
    @Override
    public ResponseEntity<IOSOrderResponse> apiV1OrdersIosPost(PostIOSOrderRequest postIOSOrderRequest) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
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
        response.setData(this.wechatOrderDTOFactory.valueOf(wechatOrderMessage));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the ios order check by order id api.
     *
     * @param id                   (required) Order id.
     * @param checkIOSOrderRequest (required) {@link CheckIOSOrderRequest}.
     * @return {@link IOSOrderResponse}.
     */
    @Override
    public ResponseEntity<IOSOrderResponse> apiV1OrdersIosIdCheckPost(String id, CheckIOSOrderRequest checkIOSOrderRequest) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

}
