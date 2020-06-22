package com.keepreal.madagascar.lemur.controller;

import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.lemur.dtoFactory.BalanceDTOFactory;
import com.keepreal.madagascar.lemur.dtoFactory.WechatOrderDTOFactory;
import com.keepreal.madagascar.lemur.service.OrderService;
import com.keepreal.madagascar.lemur.util.HttpContextUtils;
import com.keepreal.madagascar.vanga.BalanceMessage;
import com.keepreal.madagascar.vanga.WechatOrderMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import swagger.api.OrderApi;
import swagger.model.BalanceResponse;
import swagger.model.PostIOSOrderRequest;
import swagger.model.WechatOrderResponse;

/**
 * Represents the order controller.
 */
@RestController
public class OrderController implements OrderApi {

    private final OrderService orderService;
    private final WechatOrderDTOFactory wechatOrderDTOFactory;
    private final BalanceDTOFactory balanceDTOFactory;

    /**
     * Constructs the order controller.
     *
     * @param orderService          {@link OrderService}.
     * @param wechatOrderDTOFactory {@link WechatOrderDTOFactory}.
     * @param balanceDTOFactory     {@link BalanceDTOFactory}.
     */
    public OrderController(OrderService orderService,
                           WechatOrderDTOFactory wechatOrderDTOFactory,
                           BalanceDTOFactory balanceDTOFactory) {
        this.orderService = orderService;
        this.wechatOrderDTOFactory = wechatOrderDTOFactory;
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
                postIOSOrderRequest.getShellSkuId(), postIOSOrderRequest.getReceipt());

        BalanceResponse response = new BalanceResponse();
        response.setData(this.balanceDTOFactory.valueOf(balanceMessage));
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
        response.setData(this.wechatOrderDTOFactory.valueOf(wechatOrderMessage));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
