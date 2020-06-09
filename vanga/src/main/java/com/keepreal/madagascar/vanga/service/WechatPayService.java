package com.keepreal.madagascar.vanga.service;

import com.keepreal.madagascar.vanga.config.WechatPayConfiguration;
import com.keepreal.madagascar.vanga.model.WechatOrder;
import com.keepreal.madagascar.vanga.model.WechatOrderState;
import com.keepreal.madagascar.vanga.wechatPay.WXPay;
import com.keepreal.madagascar.vanga.wechatPay.WXPayConstants;
import com.keepreal.madagascar.vanga.wechatPay.WXPayUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents the wechat pay service.
 */
@Service
@Slf4j
public class WechatPayService {

    private final WXPay client;
    private final WechatPayConfiguration wechatPayConfiguration;
    private final WechatOrderService wechatOrderService;

    public WechatPayService(WXPay client,
                            WechatPayConfiguration wechatPayConfiguration,
                            WechatOrderService wechatOrderService) {
        this.client = client;
        this.wechatPayConfiguration = wechatPayConfiguration;
        this.wechatOrderService = wechatOrderService;
    }

    /**
     * Places a new wechat payment order with given metadata.
     *
     * @param userId      User id.
     * @param feeInCents  Cost in cents.
     * @param description Description.
     * @return {@link WechatOrder}.
     */
    public WechatOrder tryPlaceOrder(String userId, String feeInCents, String description) {
        String tradeNum = UUID.randomUUID().toString().replace("-", "");
        log.info(tradeNum);

        WechatOrder wechatOrder = WechatOrder.builder()
                .state(WechatOrderState.NOTPAY.getValue())
                .userId(userId)
                .description(description)
                .feeInCents(feeInCents)
                .build();

        Map<String, String> response;
        try {
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("trade_type", "APP");
            requestBody.put("out_trade_no", tradeNum);
            requestBody.put("total_fee", feeInCents);
            requestBody.put("body", description);
            requestBody.put("spbill_create_ip", this.wechatPayConfiguration.getHostIp());

            response = this.client.unifiedOrder(requestBody);

            if (response.get("return_code").equals(WXPayConstants.FAIL)) {
                wechatOrder.setErrorMessage(response.get("return_msg"));
            } else if (response.get("result_code").equals(WXPayConstants.FAIL)) {
                wechatOrder.setErrorMessage(response.get("err_code_des"));
            } else {
                wechatOrder.setPrepayId(response.get("prepay_id"));

                Map<String, String> request = new HashMap<>();
                request.put("noncestr", response.get("nonce_str"));
                request.put("prepayid", response.get("prepay_id"));
                request.put("package", "Sign=WXPay");
                request.put("timestamp", String.valueOf(wechatOrder.getCreatedTime() / 1000));
                request = this.client.fillPayRequestData(request);

                wechatOrder.setSignature(request.get("sign"));
                wechatOrder.setNonceStr(request.get("noncestr"));
                wechatOrder.setCreatedTime(Integer.parseInt(request.get("timestamp")) * 1000L);
            }
            return wechatOrder;
        } catch (Exception e) {
            wechatOrder.setErrorMessage(e.getMessage());
            return null;
        } finally {
            this.wechatOrderService.insert(wechatOrder);
        }
    }

    /**
     * Tries to close an in progress order.
     *
     * @param wechatOrder {@link WechatOrder}.
     */
    public void tryCloseOrder(WechatOrder wechatOrder) {
        if (Objects.isNull(wechatOrder)
                || WechatOrderState.CLOSED.getValue() == wechatOrder.getState()) {
            return;
        }

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("out_trade_no", wechatOrder.getTradeNumber());

        try {
            Map<String, String> response = this.client.closeOrder(requestBody);

            if (response.get("return_code").equals(WXPayConstants.FAIL)) {
                wechatOrder.setErrorMessage(response.get("return_msg"));
            } else if (response.get("result_code").equals(WXPayConstants.FAIL)) {
                wechatOrder.setErrorMessage(response.get("err_code_des"));
            } else {
                wechatOrder.setState(WechatOrderState.CLOSED.getValue());
            }
            this.wechatOrderService.update(wechatOrder);
        } catch (Exception ignored) {
        }
    }

    /**
     * Queries the state of an order and update the database.
     *
     * @param wechatOrder {@link WechatOrder}.
     */
    public void tryUpdateOrder(WechatOrder wechatOrder) {
        if (Objects.isNull(wechatOrder)) {
            return;
        }

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("out_trade_no", wechatOrder.getTradeNumber());
//        requestBody.put("out_trade_no", "eb2201974ea14f17aa12831491d68c7a");

        try {
            Map<String, String> response = this.client.orderQuery(requestBody);

            if (response.get("return_code").equals(WXPayConstants.FAIL)) {
                wechatOrder.setErrorMessage(response.get("return_msg"));
            } else if (response.get("result_code").equals(WXPayConstants.FAIL)) {
                wechatOrder.setErrorMessage(response.get("err_code_des"));
            } else {
                wechatOrder.setState(WechatOrderState.valueOf(response.get("trade_state")).getValue());
                wechatOrder.setTransactionId(response.get("transactionId"));
                this.wechatOrderService.update(wechatOrder);
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * Refunds the state of an order and update the database.
     *
     * @param wechatOrder {@link WechatOrder}.
     */
    public void tryRefund(WechatOrder wechatOrder) {
        if (Objects.isNull(wechatOrder)) {
            return;
        }

        String refundNum = UUID.randomUUID().toString().replace("-", "");
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("out_trade_no", wechatOrder.getTradeNumber());
        requestBody.put("out_refund_no", refundNum);
        requestBody.put("total_fee", wechatOrder.getFeeInCents());
        requestBody.put("refund_fee", wechatOrder.getFeeInCents());

        try {
            Map<String, String> response = this.client.refund(requestBody);

            if (response.get("return_code").equals(WXPayConstants.FAIL)) {
                wechatOrder.setErrorMessage(response.get("return_msg"));
            } else if (response.get("result_code").equals(WXPayConstants.FAIL)) {
                wechatOrder.setErrorMessage(response.get("err_code_des"));
            } else {
                wechatOrder.setRefundNumber(refundNum);
            }
        } catch (Exception ignored) {
        }
    }

}