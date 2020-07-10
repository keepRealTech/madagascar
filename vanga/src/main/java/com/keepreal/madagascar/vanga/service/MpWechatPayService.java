package com.keepreal.madagascar.vanga.service;

import com.keepreal.madagascar.vanga.config.MpWechatPayConfiguration;
import com.keepreal.madagascar.vanga.model.WechatOrder;
import com.keepreal.madagascar.vanga.model.WechatOrderState;
import com.keepreal.madagascar.vanga.wechatPay.WXPay;
import com.keepreal.madagascar.vanga.wechatPay.WXPayConstants;
import com.keepreal.madagascar.vanga.wechatPay.WXPayUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
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
                .userId(userId)
                .tradeNumber(tradeNum)
                .memberShipSkuId(shellSkuId)
                .shellSkuId(shellSkuId)
                .description(description)
                .feeInCents(feeInCents)
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

}