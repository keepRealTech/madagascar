package com.keepreal.madagascar.vanga.factory;

import com.keepreal.madagascar.vanga.WechatOrderMessage;
import com.keepreal.madagascar.vanga.WechatOrderState;
import com.keepreal.madagascar.vanga.config.WechatPayConfiguration;
import com.keepreal.madagascar.vanga.model.WechatOrder;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Represents the wechat order message factory.
 */
@Component
public class WechatOrderMessageFactory {

    private final WechatPayConfiguration wechatPayConfiguration;

    /**
     * Constructs the wechat order message factory.
     *
     * @param wechatPayConfiguration {@link WechatPayConfiguration}.
     */
    public WechatOrderMessageFactory(WechatPayConfiguration wechatPayConfiguration) {
        this.wechatPayConfiguration = wechatPayConfiguration;
    }

    /**
     * Converts {@link WechatOrder} into {@link WechatOrderMessage}.
     *
     * @param wechatOrder {@link WechatOrder}.
     * @return {@link WechatOrderMessage}.
     */
    public WechatOrderMessage valueOf(WechatOrder wechatOrder) {
        if (Objects.isNull(wechatOrder)) {
            return null;
        }

        return  WechatOrderMessage.newBuilder()
                .setAppId(this.wechatPayConfiguration.getAppId())
                .setPartnerId(this.wechatPayConfiguration.getMchId())
                .setNonceStr(wechatOrder.getNonceStr())
                .setTimestamp(wechatOrder.getCreatedTime())
                .setPrepayId(wechatOrder.getPrepayId())
                .setSignature(wechatOrder.getSignature())
                .setUserId(wechatOrder.getUserId())
                .setFeeInCents(Long.parseLong(wechatOrder.getFeeInCents()))
                .setState(this.convert(wechatOrder.getState()))
                .build();
    }

    /**
     * Converts the value of {@link WechatOrderState} into {@link WechatOrderState}.
     *
     * @param wechatOrderState Value of {@link WechatOrderState}.
     * @return {@link WechatOrderState}.
     */
    private WechatOrderState convert(Integer wechatOrderState) {
        if (Objects.isNull(wechatOrderState)) {
            return null;
        }

        switch (wechatOrderState) {
            case 1:
                return WechatOrderState.WECHAT_ORDER_STATE_NOTPAY;
            case 2:
                return WechatOrderState.WECHAT_ORDER_STATE_USERPAYING;
            case 3:
                return WechatOrderState.WECHAT_ORDER_STATE_SUCCESS;
            case 4:
                return WechatOrderState.WECHAT_ORDER_STATE_CLOSED;
            case 5:
                return WechatOrderState.WECHAT_ORDER_STATE_REFUND;
            case 6:
                return WechatOrderState.WECHAT_ORDER_STATE_PAYERROR;
            case 7:
                return WechatOrderState.WECHAT_ORDER_STATE_REVOKED;
            case 8:
                return WechatOrderState.WECHAT_ORDER_STATE_REFUNDED;
            case 0:
            default:
                return WechatOrderState.WECHAT_ORDER_STATE_UNKNOWN;
        }
    }

}
