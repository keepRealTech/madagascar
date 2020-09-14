package com.keepreal.madagascar.lemur.dtoFactory;

import com.keepreal.madagascar.common.AlipayOrderMessage;
import com.keepreal.madagascar.common.WechatOrderMessage;
import com.keepreal.madagascar.common.WechatOrderState;
import org.springframework.stereotype.Component;
import swagger.model.AlipayOrderDTO;
import swagger.model.OrderState;
import swagger.model.WechatOrderDTO;

import java.util.Objects;

/**
 * Represents the wechat order dto factory.
 */
@Component
public class OrderDTOFactory {

    /**
     * Converts {@link WechatOrderMessage} into {@link WechatOrderDTO}.
     *
     * @param wechatOrder {@link WechatOrderMessage}.
     * @return {@link WechatOrderDTO}.
     */
    public WechatOrderDTO wechatOrderValueOf(WechatOrderMessage wechatOrder) {
        if (Objects.isNull(wechatOrder)) {
            return null;
        }

        WechatOrderDTO wechatOrderDTO = new WechatOrderDTO();
        wechatOrderDTO.setId(wechatOrder.getId());
        wechatOrderDTO.setAppId(wechatOrder.getAppId());
        wechatOrderDTO.setPartnerId(wechatOrder.getPartnerId());
        wechatOrderDTO.setNonceStr(wechatOrder.getNonceStr());
        wechatOrderDTO.setTimestamp(wechatOrder.getTimestamp());
        wechatOrderDTO.setPrepayId(wechatOrder.getPrepayId());
        wechatOrderDTO.setSignature(wechatOrder.getSignature());
        wechatOrderDTO.setUserId(wechatOrder.getUserId());
        wechatOrderDTO.setFeeInCents(wechatOrder.getFeeInCents());
        wechatOrderDTO.setState(this.convert(wechatOrder.getState()));
        wechatOrderDTO.setMwebUrl("");

        return wechatOrderDTO;
    }

    /**
     * Converts {@link AlipayOrderMessage} into {@link AlipayOrderDTO}.
     *
     * @param alipayOrder {@link AlipayOrderMessage}.
     * @return {@link AlipayOrderDTO}.
     */
    public AlipayOrderDTO alipayOrderValueOf(AlipayOrderMessage alipayOrder) {
        if (Objects.isNull(alipayOrder)) {
            return null;
        }

        AlipayOrderDTO alipayOrderDTO = new AlipayOrderDTO();
        alipayOrderDTO.setId(alipayOrder.getId());
        alipayOrderDTO.setFeeInCents(alipayOrder.getFeeInCents());
        alipayOrderDTO.setState(this.convert(alipayOrder.getState()));
        alipayOrderDTO.setOrderString(alipayOrder.getOrderString());

        return alipayOrderDTO;
    }

    /**
     * Converts the {@link WechatOrderState} into {@link OrderState}.
     *
     * @param wechatOrderState {@link WechatOrderState}.
     * @return {@link OrderState}.
     */
    private OrderState convert(com.keepreal.madagascar.common.OrderState wechatOrderState) {
        if (Objects.isNull(wechatOrderState)) {
            return null;
        }

        switch (wechatOrderState) {
            case ORDER_STATE_NOTPAY:
                return OrderState.NOTPAY;
            case ORDER_STATE_USERPAYING:
                return OrderState.USERPAYING;
            case ORDER_STATE_PAYERROR:
                return OrderState.PAYERROR;
            case ORDER_STATE_REVOKED:
                return OrderState.REVOKED;
            case ORDER_STATE_REFUND:
                return OrderState.REFUND;
            case ORDER_STATE_SUCCESS:
                return OrderState.SUCCESS;
            case ORDER_STATE_CLOSED:
                return OrderState.CLOSED;
            case ORDER_STATE_UNKNOWN:
            default:
                return null;
        }
    }

}