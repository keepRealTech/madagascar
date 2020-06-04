package com.keepreal.madagascar.vanga.service;

import com.keepreal.madagascar.vanga.config.WechatPayConfiguration;
import com.keepreal.madagascar.vanga.model.WechatOrder;
import com.keepreal.madagascar.vanga.model.WechatOrderState;
import com.keepreal.madagascar.vanga.wechatPay.WXPay;
import com.keepreal.madagascar.vanga.wechatPay.WXPayConstants;
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
     * @param feeInCents  Cost in cents.
     * @param description Description.
     * @param attachment  Attachment for callback.
     * @return {@link WechatOrder}.
     */
    public WechatOrder tryPlaceOrder(String feeInCents, String description, String attachment) {
        String tradeNum = UUID.randomUUID().toString().replace("-", "");

        WechatOrder wechatOrder = WechatOrder.builder()
                .state(WechatOrderState.NOTPAY.getValue())
                .description(description)
                .feeInCents(feeInCents)
                .attachment(attachment)
                .createdTime(Instant.now().toEpochMilli())
                .build();

        Map<String, String> response;
        try {
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("trade_type", "APP");
            requestBody.put("out_trade_no", tradeNum);
            requestBody.put("total_fee", feeInCents);
            requestBody.put("body", description);
            requestBody.put("attach", attachment);
            requestBody.put("spbill_create_ip", this.wechatPayConfiguration.getHostIp());

            response = this.client.unifiedOrder(requestBody);

            if (response.get("return_code").equals(WXPayConstants.FAIL)) {
                wechatOrder.setErrorMessage(response.get("return_msg"));
            } else if (response.get("result_code").equals(WXPayConstants.FAIL)) {
                wechatOrder.setErrorMessage(response.get("err_code_des"));
            } else {
                wechatOrder.setPrepayId(response.get("prepay_id"));
                wechatOrder.setSignature(response.get("sign"));
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

        try {
            Map<String, String> response = this.client.orderQuery(requestBody);

            if (response.get("return_code").equals(WXPayConstants.FAIL)) {
                wechatOrder.setErrorMessage(response.get("return_msg"));
            } else if (response.get("result_code").equals(WXPayConstants.FAIL)) {
                wechatOrder.setErrorMessage(response.get("err_code_des"));
            } else {
                wechatOrder.setState(WechatOrderState.valueOf(response.get("trade_state")).getValue());
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