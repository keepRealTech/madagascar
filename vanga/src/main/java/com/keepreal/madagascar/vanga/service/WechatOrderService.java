package com.keepreal.madagascar.vanga.service;

import com.keepreal.madagascar.common.snowflake.generator.LongIdGenerator;
import com.keepreal.madagascar.vanga.model.WechatOrder;
import com.keepreal.madagascar.vanga.model.OrderType;
import com.keepreal.madagascar.vanga.repository.AlipayOrderRepository;
import com.keepreal.madagascar.vanga.repository.WechatOrderRepository;
import org.springframework.stereotype.Service;

/**
 * Represents the wechat order service.
 */
@Service
public class WechatOrderService {

    private final LongIdGenerator idGenerator;
    private final WechatOrderRepository wechatOrderRepository;

    /**
     * Constructs the alipay order service.
     *
     * @param idGenerator           {@link LongIdGenerator}.
     * @param wechatOrderRepository {@link WechatOrderRepository}.
     */
    public WechatOrderService(LongIdGenerator idGenerator,
                              WechatOrderRepository wechatOrderRepository) {
        this.idGenerator = idGenerator;
        this.wechatOrderRepository = wechatOrderRepository;
    }

    /**
     * Inserts the wechat order.
     *
     * @param order {@link WechatOrder}.
     * @return {@link WechatOrder}.
     */
    public WechatOrder insert(WechatOrder order) {
        order.setId(String.valueOf(this.idGenerator.nextId()));
        return this.wechatOrderRepository.save(order);
    }

    /**
     * Retrieves wechat order by order id.
     *
     * @param id Order id.
     * @return {@link WechatOrder}.
     */
    public WechatOrder retrieveById(String id) {
        return this.wechatOrderRepository.findByIdAndDeletedIsFalse(id);
    }

    /**
     * Retrieves wechat order by order trade number.
     *
     * @param tradeNumber Order trade number.
     * @return {@link WechatOrder}.
     */
    public WechatOrder retrieveByTradeNumber(String tradeNumber) {
        return this.wechatOrderRepository.findTopByTradeNumberAndDeletedIsFalse(tradeNumber);
    }

    /**
     * Update the wechat order.
     *
     * @param order {@link WechatOrder}.
     * @return {@link WechatOrder}.
     */
    public WechatOrder update(WechatOrder order) {
        return this.wechatOrderRepository.save(order);
    }

    /**
     * Retrieves the wechat order for question payment.
     *
     * @param feedId Feed id.
     * @return {@link WechatOrder}.
     */
    public WechatOrder retrieveByQuestionId(String feedId) {
        return this.wechatOrderRepository.findByPropertyIdAndTypeAndDeletedIsFalse(feedId, OrderType.PAYQUESTION.getValue());
    }

    /**
     * merge user wechat order
     *
     * @param wechatUserId      wechat user id
     * @param webMobileUserId   mobile user id
     */
    public void mergeUserWechatOrder(String wechatUserId, String webMobileUserId) {
        this.wechatOrderRepository.mergeUserWechatOrder(wechatUserId, webMobileUserId);
    }

}
