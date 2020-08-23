package com.keepreal.madagascar.vanga.service;

import com.keepreal.madagascar.vanga.config.WechatPayConfiguration;
import com.keepreal.madagascar.vanga.model.WechatOrder;
import com.keepreal.madagascar.vanga.model.WechatOrderState;
import com.keepreal.madagascar.vanga.model.WechatOrderType;
import com.keepreal.madagascar.vanga.wechatPay.WXPay;
import com.keepreal.madagascar.vanga.wechatPay.WXPayConstants;
import com.keepreal.madagascar.vanga.wechatPay.WXPayUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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

    /**
     * Constructs the wechat pay service.
     *
     * @param client                 {@link WXPay}.
     * @param wechatPayConfiguration {@link WechatPayConfiguration}.
     * @param wechatOrderService     {@link WechatOrderService}.
     */
    public WechatPayService(@Qualifier("wechatpay") WXPay client,
                            WechatPayConfiguration wechatPayConfiguration,
                            WechatOrderService wechatOrderService) {
        this.client = client;
        this.wechatPayConfiguration = wechatPayConfiguration;
        this.wechatOrderService = wechatOrderService;
    }

    /**
     * Places a new wechat payment order with given metadata.
     *
     * @param userId          User id.
     * @param feeInCents      Cost in cents.
     * @param propertyId      Property id.
     * @param wechatOrderType {@link WechatOrderType}.
     * @return {@link WechatOrder}.
     */
    public WechatOrder tryPlaceOrder(String userId,
                                     String feeInCents,
                                     String propertyId,
                                     WechatOrderType wechatOrderType) {
        String tradeNum = UUID.randomUUID().toString().replace("-", "");

        String description = String.format("Type:[%s], Id:[%s]", wechatOrderType.name(), propertyId);

        WechatOrder wechatOrder = WechatOrder.builder()
                .state(WechatOrderState.NOTPAY.getValue())
                .userId(userId)
                .appId(this.wechatPayConfiguration.getAppId())
                .mchId(this.wechatPayConfiguration.getMchId())
                .tradeNumber(tradeNum)
                .propertyId(propertyId)
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

            log.info(requestBody.toString());

            response = this.client.unifiedOrder(requestBody);

            log.info(response.toString());

            if (response.get("return_code").equals(WXPayConstants.FAIL)) {
                wechatOrder.setErrorMessage(response.get("return_msg"));
                wechatOrder.setCreatedTime(WXPayUtil.getCurrentTimestampMs());
                this.wechatOrderService.insert(wechatOrder);
                return null;
            } else if (response.get("result_code").equals(WXPayConstants.FAIL)) {
                wechatOrder.setErrorMessage(response.get("err_code_des"));
                wechatOrder.setCreatedTime(WXPayUtil.getCurrentTimestampMs());
                this.wechatOrderService.insert(wechatOrder);
                return null;
            }

            Map<String, String> request = new HashMap<>();
            request.put("noncestr", response.get("nonce_str"));
            request.put("prepayid", response.get("prepay_id"));
            request.put("package", "Sign=WXPay");
            request.put("timestamp", String.valueOf(WXPayUtil.getCurrentTimestamp()));
            request = this.client.fillPayRequestData(request);

            wechatOrder.setCreatedTime(Integer.parseInt(request.get("timestamp")) * 1000L);
            wechatOrder = this.wechatOrderService.insert(wechatOrder);

            wechatOrder.setPrepayId(response.get("prepay_id"));
            wechatOrder.setSignature(request.get("sign"));
            wechatOrder.setNonceStr(request.get("noncestr"));

            return wechatOrder;
        } catch (Exception e) {
            wechatOrder.setErrorMessage(e.getMessage());
            wechatOrder.setCreatedTime(WXPayUtil.getCurrentTimestampMs());
            this.wechatOrderService.insert(wechatOrder);
            return null;
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
     * @return {@link WechatOrder}.
     */
    public WechatOrder tryUpdateOrder(WechatOrder wechatOrder) {
        if (Objects.isNull(wechatOrder)
                || (WechatOrderState.NOTPAY.getValue() != wechatOrder.getState()
                && WechatOrderState.USERPAYING.getValue() != wechatOrder.getState())) {
            return wechatOrder;
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
                wechatOrder.setTransactionId(response.get("transaction_id"));
            }
            return this.wechatOrderService.update(wechatOrder);
        } catch (Exception ignored) {
        }

        return null;
    }

    /**
     * Implements the callback logic.
     *
     * @param callbackPayload Payload.
     * @return {@link WechatOrder}.
     */
    public WechatOrder orderCallback(String callbackPayload) {
        if (StringUtils.isEmpty(callbackPayload)) {
            return null;
        }

        try {
            Map<String, String> response = this.client.processResponseXml(callbackPayload);

            if (response.get("return_code").equals(WXPayConstants.FAIL)) {
                return null;
            }

            WechatOrder wechatOrder = this.wechatOrderService.retrieveByTradeNumber(response.get("out_trade_no"));

            if (Objects.isNull(wechatOrder)
                    || wechatOrder.getState() == WechatOrderState.SUCCESS.getValue()
                    || wechatOrder.getState() == WechatOrderState.PAYERROR.getValue()) {
                return null;
            }

            if (response.get("result_code").equals(WXPayConstants.FAIL)) {
                wechatOrder.setState(WechatOrderState.PAYERROR.getValue());
                wechatOrder.setErrorMessage(response.get("err_code_des"));
            } else {
                wechatOrder.setState(WechatOrderState.SUCCESS.getValue());
                wechatOrder.setTransactionId(response.get("transaction_id"));
            }
            return this.wechatOrderService.update(wechatOrder);
        } catch (Exception ignored) {
        }

        return null;
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