package com.keepreal.madagascar.vanga.service;

import com.keepreal.madagascar.common.SceneType;
import com.keepreal.madagascar.vanga.config.WechatPayConfiguration;
import com.keepreal.madagascar.vanga.model.WechatOrder;
import com.keepreal.madagascar.vanga.model.OrderState;
import com.keepreal.madagascar.vanga.model.OrderType;
import com.keepreal.madagascar.common.wechat_pay.WXPay;
import com.keepreal.madagascar.common.wechat_pay.WXPayConstants;
import com.keepreal.madagascar.common.wechat_pay.WXPayUtil;
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

    private static final String IOS_SCENE_INFO = "{\"h5_info\": {\"type\":\"IOS\",\"app_name\":\"tiaodao\",\"bundle_id\":\"cn.keepreal.feeds\"}}";
    private static final String ANDROID_SCENE_INFO = "{\"h5_info\": {\"type\":\"Android\",\"app_name\":\"tiaodao\",\"package_name\":\"com.bcfg.client\"}}";
    private static final String WAP_SCENE_INFO = "{\"h5_info\": {\"type\":\"Wap\",\"wap_url\":\"https://tiaodaoapp.com\",\"wap_name\":\"跳岛首页\"}}";
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
     * @param orderType {@link OrderType}.
     * @param sceneType       {@link SceneType}.
     * @return {@link WechatOrder}.
     */
    public WechatOrder tryPlaceOrder(String userId,
                                     String feeInCents,
                                     String propertyId,
                                     OrderType orderType,
                                     SceneType sceneType,
                                     String remoteIp,
                                     String description) {
        String tradeNum = UUID.randomUUID().toString().replace("-", "");

        description = StringUtils.isEmpty(description) ? String.format("Type:[%s], Id:[%s]", orderType.name(), propertyId) : description;

        boolean isH5 = this.isH5Pay(orderType);

        if (isH5 && (Objects.isNull(sceneType) || SceneType.SCENE_NONE.equals(sceneType))) {
            log.error("Invalid scene type for h5 wechat pay.");
            return null;
        }

        WechatOrder wechatOrder = WechatOrder.builder()
                .state(OrderState.NOTPAY.getValue())
                .userId(userId)
                .appId(this.wechatPayConfiguration.getAppId())
                .mchId(this.wechatPayConfiguration.getMchId())
                .tradeNumber(tradeNum)
                .propertyId(propertyId)
                .description(description)
                .feeInCents(feeInCents)
                .type(orderType.getValue())
                .build();

        Map<String, String> response;
        String tradeType = isH5 ? "MWEB" : "APP";
        try {
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("trade_type", tradeType);
            requestBody.put("out_trade_no", tradeNum);
            requestBody.put("total_fee", feeInCents);
            requestBody.put("body", description);
            requestBody.put("spbill_create_ip", remoteIp);

            if (isH5) {
                String sceneInfo;
                switch (sceneType) {
                    case SCENE_IOS:
                        sceneInfo = WechatPayService.IOS_SCENE_INFO;
                        break;
                    case SCENE_WAP:
                        sceneInfo = WechatPayService.WAP_SCENE_INFO;
                        break;
                    case SCENE_ANDROID:
                        sceneInfo = WechatPayService.ANDROID_SCENE_INFO;
                        break;
                    default:
                        return null;
                }
                requestBody.put("scene_info", sceneInfo);
            }

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

            wechatOrder.setPrepayId(request.get("prepayid"));
            wechatOrder.setSignature(request.get("sign"));
            wechatOrder.setNonceStr(request.get("noncestr"));

            wechatOrder.setMwebUrl(response.getOrDefault("mweb_url", ""));

            return wechatOrder;
        } catch (Exception e) {
            wechatOrder.setErrorMessage(e.getMessage());
            wechatOrder.setCreatedTime(WXPayUtil.getCurrentTimestampMs());
            wechatOrder.setState(OrderState.CLOSED.getValue());
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
                || OrderState.CLOSED.getValue() == wechatOrder.getState()) {
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
                wechatOrder.setState(OrderState.CLOSED.getValue());
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
                || (OrderState.NOTPAY.getValue() != wechatOrder.getState()
                && OrderState.USERPAYING.getValue() != wechatOrder.getState())) {
            return wechatOrder;
        }

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("out_trade_no", wechatOrder.getTradeNumber());

        try {
            Map<String, String> response = this.client.orderQuery(requestBody);

            log.info(response.toString());

            if (response.get("return_code").equals(WXPayConstants.FAIL)) {
                wechatOrder.setErrorMessage(response.get("return_msg"));
            } else if (response.get("result_code").equals(WXPayConstants.FAIL)) {
                wechatOrder.setErrorMessage(response.get("err_code_des"));
            } else if (response.get("trade_state").equals(WXPayConstants.SUCCESS)){
                wechatOrder.setState(OrderState.valueOf(response.get("trade_state")).getValue());
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
                    || wechatOrder.getState() == OrderState.SUCCESS.getValue()
                    || wechatOrder.getState() == OrderState.PAYERROR.getValue()) {
                return null;
            }

            if (response.get("result_code").equals(WXPayConstants.FAIL)) {
                wechatOrder.setState(OrderState.PAYERROR.getValue());
                wechatOrder.setErrorMessage(response.get("err_code_des"));
            } else {
                wechatOrder.setState(OrderState.SUCCESS.getValue());
                wechatOrder.setTransactionId(response.get("transaction_id"));
            }
            return this.wechatOrderService.update(wechatOrder);
        } catch (Exception ignored) {
        }

        return null;
    }

    /**
     * Implements the refund callback logic.
     *
     * @param callbackPayload Payload.
     * @return {@link WechatOrder}.
     */
    public WechatOrder refundCallback(String callbackPayload) {
        if (StringUtils.isEmpty(callbackPayload)) {
            return null;
        }

        try {
            Map<String, String> response = this.client.processRefundResponseXml(callbackPayload);

            if (response.get("return_code").equals(WXPayConstants.FAIL)) {
                return null;
            }

            WechatOrder wechatOrder = this.wechatOrderService.retrieveByTradeNumber(response.get("out_trade_no"));

            if (Objects.isNull(wechatOrder)
                    || wechatOrder.getState() != OrderState.REFUNDING.getValue()) {
                return null;
            }

            if (WXPayConstants.FAIL.equals(response.get("result_code"))
                    || !WXPayConstants.SUCCESS.equals(response.get("refund_status"))) {
                wechatOrder.setState(OrderState.PAYERROR.getValue());
                wechatOrder.setErrorMessage(response.get("refund_status"));
            } else {
                wechatOrder.setState(OrderState.REFUNDED.getValue());
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
                wechatOrder.setErrorMessage(response.get("return_msg"));
            } else if (response.get("result_code").equals(WXPayConstants.FAIL)) {
                wechatOrder.setErrorMessage(response.get("err_code_des"));
            } else {
                wechatOrder.setState(OrderState.REFUNDING.getValue());
            }
        } catch (Exception e) {
            wechatOrder.setErrorMessage(e.getMessage());
        } finally {
            wechatOrder = this.wechatOrderService.update(wechatOrder);
        }

        return wechatOrder;
    }

    /**
     * Checks if it is H5 pay.
     *
     * @param type {@link OrderType}.
     * @return True if it is h5 payment.
     */
    private boolean isH5Pay(OrderType type) {
        return OrderType.PAYMEMBERSHIPH5.equals(type) || OrderType.PAYSUPPORTH5.equals(type);
    }

}