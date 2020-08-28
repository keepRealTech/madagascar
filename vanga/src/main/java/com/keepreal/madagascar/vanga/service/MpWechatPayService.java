package com.keepreal.madagascar.vanga.service;

import com.keepreal.madagascar.vanga.config.MpWechatPayConfiguration;
import com.keepreal.madagascar.vanga.model.WechatOrder;
import com.keepreal.madagascar.vanga.model.WechatOrderState;
import com.keepreal.madagascar.vanga.model.WechatOrderType;
import com.keepreal.madagascar.common.wechat_pay.WXPay;
import com.keepreal.madagascar.common.wechat_pay.WXPayConstants;
import com.keepreal.madagascar.common.wechat_pay.WXPayUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents the wechat pay service.
 */
@Service
@Slf4j
public class MpWechatPayService {

    private final WXPay client;
    private final MpWechatPayConfiguration mpWechatPayConfiguration;
    private final WechatOrderService wechatOrderService;

    /**
     * Constructs the mp wechat pay service.
     *
     * @param client                   {@link WXPay}.
     * @param mpWechatPayConfiguration {@link MpWechatPayConfiguration}.
     * @param wechatOrderService       {@link WechatOrderService}.
     */
    public MpWechatPayService(@Qualifier("mpwechatpay") WXPay client,
                              MpWechatPayConfiguration mpWechatPayConfiguration,
                              WechatOrderService wechatOrderService) {
        this.client = client;
        this.mpWechatPayConfiguration = mpWechatPayConfiguration;
        this.wechatOrderService = wechatOrderService;
    }

    /**
     * Places a new wechat payment order with given metadata.
     *
     * @param userId     User id.
     * @param feeInCents Cost in cents.
     * @param shellSkuId Shell sku id.
     * @param openId     Open id.
     * @return {@link WechatOrder}.
     */
    public WechatOrder tryPlaceOrder(String userId,
                                     String feeInCents,
                                     String shellSkuId,
                                     String openId) {
        String tradeNum = UUID.randomUUID().toString().replace("-", "");

        String description = String.format("购买贝壳%s", shellSkuId);

        WechatOrder wechatOrder = WechatOrder.builder()
                .state(WechatOrderState.NOTPAY.getValue())
                .appId(this.mpWechatPayConfiguration.getAppId())
                .mchId(this.mpWechatPayConfiguration.getMchId())
                .userId(userId)
                .tradeNumber(tradeNum)
                .propertyId(shellSkuId)
                .description(description)
                .feeInCents(feeInCents)
                .type(WechatOrderType.PAYSHELL.getValue())
                .build();

        Map<String, String> response;
        try {
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("trade_type", "JSAPI");
            requestBody.put("out_trade_no", tradeNum);
            requestBody.put("total_fee", feeInCents);
            requestBody.put("body", description);
            requestBody.put("spbill_create_ip", this.mpWechatPayConfiguration.getHostIp());
            requestBody.put("openid", openId);

            response = this.client.unifiedOrder(requestBody);

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
            request.put("nonceStr", response.get("nonce_str"));
            request.put("package", String.format("prepay_id=%s", response.get("prepay_id")));
            request.put("timeStamp", String.valueOf(WXPayUtil.getCurrentTimestamp()));
            request = this.client.fillMPPayRequestData(request);

            log.info(request.toString());

            wechatOrder.setCreatedTime(Integer.parseInt(request.get("timeStamp")) * 1000L);
            wechatOrder = this.wechatOrderService.insert(wechatOrder);

            wechatOrder.setPrepayId(response.get("prepay_id"));
            wechatOrder.setSignature(request.get("sign"));
            wechatOrder.setNonceStr(request.get("nonceStr"));

            log.info(wechatOrder.toString());

            return wechatOrder;
        } catch (Exception e) {
            wechatOrder.setErrorMessage(e.getMessage());
            wechatOrder.setCreatedTime(WXPayUtil.getCurrentTimestampMs());
            this.wechatOrderService.insert(wechatOrder);
            return null;
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

}