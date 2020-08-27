package com.keepreal.madagascar.workflow.settler.service;

import com.keepreal.madagascar.common.wechat_pay.WXPay;
import com.keepreal.madagascar.common.wechat_pay.WXPayConstants;
import com.keepreal.madagascar.common.wechat_pay.WXPayUtil;
import com.keepreal.madagascar.workflow.settler.config.WechatPayConfiguration;
import com.keepreal.madagascar.workflow.settler.model.WechatOrder;
import com.keepreal.madagascar.workflow.settler.model.WechatOrderState;
import com.keepreal.madagascar.workflow.settler.model.WechatOrderType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
    private final WechatOrderService wechatOrderService;

    /**
     * Constructs the wechat pay service.
     *
     * @param client                 {@link WXPay}.
     * @param wechatOrderService     {@link WechatOrderService}.
     */
    public WechatPayService(@Qualifier("wechatpay") WXPay client,
                            WechatOrderService wechatOrderService) {
        this.client = client;
        this.wechatOrderService = wechatOrderService;
    }

    /**
     * Refunds the state of an order and update the database.
     *
     * @param wechatOrder {@link WechatOrder}.
     * @param reason      Refund reason.
     * @return {@link WechatOrder}.
     */
    public WechatOrder tryRefund(WechatOrder wechatOrder, String reason) {
        if (Objects.isNull(wechatOrder)) {
            return wechatOrder;
        }

        String refundNum = UUID.randomUUID().toString().replace("-", "");
        wechatOrder.setRefundNumber(refundNum);
        wechatOrder.setRefundInCents(wechatOrder.getFeeInCents());
        wechatOrder.setRefundReason(reason);
        wechatOrder.setRefundTime(Instant.now().toEpochMilli());

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("out_trade_no", wechatOrder.getTradeNumber());
        requestBody.put("out_refund_no", refundNum);
        requestBody.put("total_fee", wechatOrder.getFeeInCents());
        requestBody.put("refund_fee", wechatOrder.getFeeInCents());

        try {
            Map<String, String> response = this.client.refund(requestBody);

            if (response.get("return_code").equals(WXPayConstants.FAIL)) {
                throw new Exception(response.get("return_msg"));
            } else if (response.get("result_code").equals(WXPayConstants.FAIL)) {
                throw new Exception(response.get("err_code_des"));
            } else {
                wechatOrder.setState(WechatOrderState.REFUNDING.getValue());
            }
        } catch (Exception e) {
            wechatOrder.setErrorMessage(e.getMessage());
            throw new RuntimeException("Failed apply refund.");
        } finally {
            wechatOrder = this.wechatOrderService.update(wechatOrder);
        }

        return wechatOrder;
    }

}