package com.keepreal.madagascar.vanga.service;

import com.alipay.easysdk.factory.Factory;
import com.alipay.easysdk.kernel.Config;
import com.alipay.easysdk.kernel.util.Signer;
import com.alipay.easysdk.payment.app.models.AlipayTradeAppPayResponse;
import com.alipay.easysdk.payment.common.models.AlipayTradeQueryResponse;
import com.alipay.easysdk.payment.wap.models.AlipayTradeWapPayResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.keepreal.madagascar.vanga.config.AlipayConfiguration;
import com.keepreal.madagascar.vanga.model.AlipayOrder;
import com.keepreal.madagascar.vanga.model.OrderState;
import com.keepreal.madagascar.vanga.model.OrderType;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents the alipay service.
 */
@Service
@Slf4j
public class AlipayService {

    private final AlipayConfiguration alipayConfiguration;
    private final AlipayOrderService alipayOrderService;
    private final Gson gson;

    /**
     * Constructs the alipay service.
     *
     * @param alipayConfiguration {@link AlipayConfiguration}.
     * @param alipayOrderService  {@link AlipayOrderService}.
     */
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

//        Factory.setOptions(config);
    }

    /**
     * Tries to generate an order string for alipay.
     *
     * @param userId      User id.
     * @param propertyId  Property id.
     * @param feeInCents  Fee in cents.
     * @param description Description.
     * @param orderType   Order type.
     * @return {@link AlipayOrder}.
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
                .appId(this.alipayConfiguration.getAppId())
                .createdTime(Instant.now().toEpochMilli())
                .build();

        try {
            AlipayTradeAppPayResponse response = Factory.Payment.App().pay(description, tradeNum, this.convertCentsToYuan(feeInCents));

            alipayOrder = this.alipayOrderService.insert(alipayOrder);
            alipayOrder.setOrderString(response.body);
        } catch (Exception exception) {
            alipayOrder.setErrorMessage(exception.toString());
            alipayOrder.setState(OrderState.CLOSED.getValue());
            this.alipayOrderService.insert(alipayOrder);
            return null;
        }

        return alipayOrder;
    }

    /**
     * Tries to generate an order string for alipay.
     *
     * @param userId      User id.
     * @param propertyId  Property id.
     * @param feeInCents  Fee in cents.
     * @param description Description.
     * @param orderType   Order type.
     * @return {@link AlipayOrder}.
     */
    @SneakyThrows
    public AlipayOrder tryPlaceOrderH5(String userId,
                                       String propertyId,
                                       String feeInCents,
                                       String description,
                                       OrderType orderType,
                                       String returnUrl,
                                       String quitUrl) {
        String tradeNum = UUID.randomUUID().toString().replace("-", "");

        AlipayOrder alipayOrder = AlipayOrder.builder()
                .state(OrderState.NOTPAY.getValue())
                .userId(userId)
                .tradeNumber(tradeNum)
                .propertyId(propertyId)
                .description(description)
                .feeInCents(feeInCents)
                .type(orderType.getValue())
                .appId(this.alipayConfiguration.getAppId())
                .createdTime(Instant.now().toEpochMilli())
                .build();

        try {
            AlipayTradeWapPayResponse response = Factory.Payment.Wap().pay(
                    description,
                    tradeNum,
                    this.convertCentsToYuan(feeInCents),
                    quitUrl,
                    returnUrl);

            alipayOrder = this.alipayOrderService.insert(alipayOrder);
            alipayOrder.setOrderString(response.body);
        } catch (Exception exception) {
            alipayOrder.setErrorMessage(exception.toString());
            alipayOrder.setState(OrderState.CLOSED.getValue());
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
        Type type = new TypeToken<Map<String, String>>() {}.getType();
        Map<String, String> paramMap = this.gson.fromJson(callbackPayload, type);

        AlipayOrder alipayOrder = this.verifySignature(paramMap);

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
            alipayOrder.setTransactionId(paramMap.get("trade_no"));
            alipayOrder.setState(OrderState.SUCCESS.getValue());
        } else if ("TRADE_CLOSED".equals(paramMap.get("trade_status"))) {
            alipayOrder.setState(OrderState.CLOSED.getValue());
        }

        return this.alipayOrderService.update(alipayOrder);
    }

    /**
     * Verifies the sync notification.
     *
     * @param content  Content to sign.
     * @param sign     Sign.
     * @param paramMap Params.
     * @return {@link AlipayOrder}.
     */
    @SneakyThrows
    public AlipayOrder verifySyncNotification(String content, String sign, Map<String, String> paramMap, AlipayOrder alipayOrder) {
        boolean verified;
        if (Factory.Payment.Common()._kernel.isCertMode()) {
            verified = Signer.verify(content, sign, Factory.Payment.Common()._kernel.extractAlipayPublicKey(""));
        } else {
            verified = Signer.verify(content, sign, Factory.Payment.Common()._kernel.getConfig("alipayPublicKey"));
        }

        if (!verified) {
            return null;
        }

        long total_amount = new BigDecimal(paramMap.get("total_amount")).multiply(new BigDecimal(100)).longValue();

        if (Objects.isNull(alipayOrder)
                || !alipayOrder.getFeeInCents().equals(String.valueOf(total_amount))
                || !this.alipayConfiguration.getAppId().equals(paramMap.get("app_id"))) {
            return null;
        }

        alipayOrder.setTransactionId(paramMap.get("trade_no"));
        alipayOrder.setState(OrderState.SUCCESS.getValue());

        return this.alipayOrderService.update(alipayOrder);
    }

    /**
     * Verifies the payload and get related order.
     *
     * @param paramMap Returned payload with signature.
     * @return {@link AlipayOrder}.
     */
    @SneakyThrows
    public AlipayOrder verifySignature(Map<String, String> paramMap) {
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
        } else if ("TRADE_CLOSED".equals(response.tradeStatus)) {
            alipayOrder.setState(OrderState.CLOSED.getValue());
        }

        return this.alipayOrderService.update(alipayOrder);
    }

    /**
     * Converts the price in cents into yuans.
     *
     * @param priceInCents Price in cents.
     * @return Price in yuans.
     */
    private String convertCentsToYuan(String priceInCents) {
        return BigDecimal.valueOf(Long.parseLong(priceInCents))
                .setScale(2, BigDecimal.ROUND_DOWN)
                .divide(new BigDecimal(100), RoundingMode.FLOOR).toString();
    }

}