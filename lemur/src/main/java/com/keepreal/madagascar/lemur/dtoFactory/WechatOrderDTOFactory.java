package com.keepreal.madagascar.lemur.dtoFactory;

import com.keepreal.madagascar.vanga.WechatOrderMessage;
import com.keepreal.madagascar.vanga.WechatOrderState;
import org.springframework.stereotype.Component;
import swagger.model.OrderState;
import swagger.model.WechatOrderDTO;

import java.util.Objects;

/**
 * Represents the wechat order dto factory.
 */
@Component
public class WechatOrderDTOFactory {

    /**
     * Converts {@link WechatOrderMessage} into {@link WechatOrderDTO}.
     *
     * @param wechatOrder {@link WechatOrderMessage}.
     * @return {@link WechatOrderDTO}.
     */
    public WechatOrderDTO valueOf(WechatOrderMessage wechatOrder) {
        if (Objects.isNull(wechatOrder)) {
            return null;
        }

        WechatOrderDTO wechatOrderDTO = new WechatOrderDTO();
        wechatOrderDTO.setId(wechatOrder.getId());
        wechatOrderDTO.setAppId(wechatOrderDTO.getAppId());
        wechatOrderDTO.setPartnerId(wechatOrder.getPartnerId());
        wechatOrderDTO.setNonceStr(wechatOrder.getNonceStr());
        wechatOrderDTO.setTimestamp(wechatOrder.getTimestamp());
        wechatOrderDTO.setPrepayId(wechatOrder.getPrepayId());
        wechatOrderDTO.setSignature(wechatOrder.getSignature());
        wechatOrderDTO.setUserId(wechatOrder.getUserId());
        wechatOrderDTO.setFeeInCents(wechatOrder.getFeeInCents());
        wechatOrderDTO.setState(this.convert(wechatOrder.getState()));

        return wechatOrderDTO;
    }

    /**
     * Converts the {@link WechatOrderState} into {@link OrderState}.
     *
     * @param wechatOrderState {@link WechatOrderState}.
     * @return {@link OrderState}.
     */
    private OrderState convert(WechatOrderState wechatOrderState) {
        if (Objects.isNull(wechatOrderState)) {
            return null;
        }

        switch (wechatOrderState) {
            case WECHAT_ORDER_STATE_NOTPAY:
                return OrderState.NOTPAY;
            case WECHAT_ORDER_STATE_USERPAYING:
                return OrderState.USERPAYING;
            case WECHAT_ORDER_STATE_PAYERROR:
                return OrderState.PAYERROR;
            case WECHAT_ORDER_STATE_REVOKED:
                return OrderState.REVOKED;
            case WECHAT_ORDER_STATE_REFUND:
                return OrderState.REFUND;
            case WECHAT_ORDER_STATE_SUCCESS:
                return OrderState.SUCCESS;
            case WECHAT_ORDER_STATE_CLOSED:
                return OrderState.CLOSED;
            case WECHAT_ORDER_STATE_UNKNOWN:
            default:
                return null;
        }
    }

}