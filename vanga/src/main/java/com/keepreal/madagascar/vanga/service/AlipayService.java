package com.keepreal.madagascar.vanga.service;

import com.alipay.easysdk.factory.Factory;
import com.alipay.easysdk.kernel.Config;
import com.alipay.easysdk.payment.app.models.AlipayTradeAppPayResponse;
import com.alipay.easysdk.payment.common.models.AlipayTradeQueryResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.keepreal.madagascar.common.wechat_pay.WXPayConstants;
import com.keepreal.madagascar.vanga.config.AlipayConfiguration;
import com.keepreal.madagascar.vanga.model.AlipayOrder;
import com.keepreal.madagascar.vanga.model.OrderState;
import com.keepreal.madagascar.vanga.model.OrderType;
import com.keepreal.madagascar.vanga.model.WechatOrder;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
public class AlipayService {

    private final AlipayConfiguration alipayConfiguration;
    private final AlipayOrderService alipayOrderService;
    private final Gson gson;

    public AlipayService(AlipayConfiguration alipayConfiguration,
                         AlipayOrderService alipayOrderService) {
        this.alipayOrderService = alipayOrderService;
        this.gson = new Gson();
        this.alipayConfiguration = alipayConfiguration;

        Config config = new Config();
        config.protocol = "https";
        config.gatewayHost = "openapi.alipay.com";
        config.signType = "RSA2";
        config.appId = this.alipayConfiguration.getAppId();
        config.merchantPrivateKey = this.alipayConfiguration.getMerchantKey();
        config.notifyUrl = this.alipayConfiguration.getCallbackAddress();
        config.merchantCertPath = this.alipayConfiguration.getMerchantCertPath();
        config.alipayCertPath = this.alipayConfiguration.getAlipayCertPath();
        config.alipayRootCertPath = this.alipayConfiguration.getAlipayRootPath();

        Factory.setOptions(config);
    }

    /**
     * Tries to generate an order string for alipay.
     *
     * @param userId        User id.
     * @param propertyId    Property id.
     * @param feeInCents    Fee in cents.
     * @param description   Description.
     * @param orderType     Order type.
     * @return  {@link AlipayOrder}.
     */
    @SneakyThrows
    public AlipayOrder tryPlaceOrderApp(String userId,
                                        String propertyId,
                                        String feeInCents,
                                        String description,
                                        OrderType orderType) {
        String tradeNum = UUID.randomUUID().toString().replace("-", "");

        AlipayOrder alipayOrder = AlipayOrder.builder()
                .state(OrderState.NOTPAY.getValue())
                .userId(userId)
                .tradeNumber(tradeNum)
                .propertyId(propertyId)
                .description(description)
                .feeInCents(feeInCents)
                .type(orderType.getValue())
                .createdTime(Instant.now().toEpochMilli())
                .build();

        try {
            StringBuilder feeInYuansBuilder = new StringBuilder(feeInCents);
            feeInYuansBuilder.insert(feeInCents.length() - 2, '.');

            AlipayTradeAppPayResponse response = Factory.Payment.App().pay(description, tradeNum, feeInYuansBuilder.toString());

            alipayOrder = this.alipayOrderService.insert(alipayOrder);
            alipayOrder.setOrderString(response.body);
        } catch (Exception exception) {
            alipayOrder.setErrorMessage(exception.getMessage());
            this.alipayOrderService.insert(alipayOrder);
            return null;
        }

        return alipayOrder;
    }

    /**
     * Verifies the callback.
     *
     * @param callbackPayload Callback payload.
     * @return {@link AlipayOrder}.
     */
    @SneakyThrows
    public AlipayOrder orderCallback(String callbackPayload) {
        if (StringUtils.isEmpty(callbackPayload)) {
            return null;
        }
        Type type = new TypeToken<Map<String, String>>(){}.getType();
        Map<String, String> paramMap = this.gson.fromJson(callbackPayload, type);

        AlipayOrder alipayOrder = this.verifyReceipt(paramMap);

        long total_amount = new BigDecimal(paramMap.get("total_amount")).multiply(new BigDecimal(100)).longValue();

        if (Objects.isNull(alipayOrder)
                || OrderState.SUCCESS.getValue() == alipayOrder.getState()
                || OrderState.CLOSED.getValue() == alipayOrder.getState()
                || !alipayOrder.getFeeInCents().equals(String.valueOf(total_amount))
                || !this.alipayConfiguration.getAppId().equals(paramMap.get("app_id"))) {
            return null;
        }

        if ("TRADE_SUCCESS".equals(paramMap.get("trade_status"))
            || "TRADE_FINISHED".equals(paramMap.get("trade_status"))) {
            alipayOrder.setState(OrderState.SUCCESS.getValue());
            alipayOrder.setTransactionId(paramMap.get("trade_no"));
        } else if ("TRADE_CLOSED".equals(paramMap.get("trade_status"))){
            alipayOrder.setState(OrderState.CLOSED.getValue());
        }

        return this.alipayOrderService.update(alipayOrder);
    }

    /**
     * Verifies the payload and get related order.
     *
     * @param paramMap Returned payload with signature.
     * @return {@link AlipayOrder}.
     */
    @SneakyThrows
    public AlipayOrder verifyReceipt(Map<String, String> paramMap) {
        Boolean verified = Factory.Payment.Common().verifyNotify(paramMap);

        if (!Boolean.TRUE.equals(verified)) {
            return null;
        }

        return this.alipayOrderService.retrieveByTradeNumber(paramMap.get("out_trade_no"));
    }

    /**
     * Tries to update an alipay order.
     *
     * @param alipayOrder {@link AlipayOrder}.
     * @return {@link AlipayOrder}.
     */
    @SneakyThrows
    public AlipayOrder tryUpdateOrder(AlipayOrder alipayOrder) {
        if (Objects.isNull(alipayOrder)
                || OrderState.SUCCESS.getValue() == alipayOrder.getState()
                || OrderState.CLOSED.getValue() == alipayOrder.getState()
                || OrderState.REFUNDED.getValue() == alipayOrder.getState()
                || OrderState.REVOKED.getValue() == alipayOrder.getState()) {
            return alipayOrder;
        }

        AlipayTradeQueryResponse response = Factory.Payment.Common().query(alipayOrder.getTradeNumber());

        if (!"10000".equals(response.code)) {
            alipayOrder.setErrorMessage(response.msg + ";" + response.subCode + ";" + response.subMsg);
        } else if ("TRADE_SUCCESS".equals(response.tradeStatus)
                || "TRADE_FINISHED".equals(response.tradeStatus)) {
            alipayOrder.setState(OrderState.SUCCESS.getValue());
            alipayOrder.setTransactionId(response.tradeNo);
        } else if ("TRADE_CLOSED".equals(response.tradeStatus)){
            alipayOrder.setState(OrderState.CLOSED.getValue());
        }

        return this.alipayOrderService.update(alipayOrder);
    }


}