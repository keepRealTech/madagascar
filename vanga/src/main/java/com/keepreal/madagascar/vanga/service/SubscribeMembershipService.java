package com.keepreal.madagascar.vanga.service;

import com.keepreal.madagascar.common.snowflake.generator.LongIdGenerator;
import com.keepreal.madagascar.vanga.model.MembershipSku;
import com.keepreal.madagascar.vanga.model.Payment;
import com.keepreal.madagascar.vanga.model.PaymentState;
import com.keepreal.madagascar.vanga.model.SubscribeMembership;
import com.keepreal.madagascar.vanga.model.WechatOrder;
import com.keepreal.madagascar.vanga.model.WechatOrderState;
import com.keepreal.madagascar.vanga.repository.SubscribeMembershipRepository;
import lombok.Builder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.stereotype.Service;

import javax.persistence.Column;
import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;

/**
 * Represents the subscribe membership service.
 */
@Service
public class SubscribeMembershipService {

    private final PaymentService paymentService;
    private final SkuService membershipSkuService;
    private final SubscribeMembershipRepository subscriptionMemberRepository;
    private final LongIdGenerator idGenerator;

    /**
     * Constructor the subscribe membership service.
     *
     * @param paymentService                   {@link PaymentService}.
     * @param membershipSkuService             {@link SkuService}.
     * @param subscriptionMemberRepository     {@link SubscribeMembershipRepository}.
     * @param idGenerator                      {@link LongIdGenerator}.
     */
    public SubscribeMembershipService(PaymentService paymentService,
                                      SkuService membershipSkuService,
                                      SubscribeMembershipRepository subscriptionMemberRepository,
                                      LongIdGenerator idGenerator) {
        this.paymentService = paymentService;
        this.membershipSkuService = membershipSkuService;
        this.subscriptionMemberRepository = subscriptionMemberRepository;
        this.idGenerator = idGenerator;
    }

    /**
     * retrieve the membership count by island id.
     *
     * @param islandId island id.
     * @return member count.
     */
    public Integer getMemberCountByIslandId(String islandId) {
        LocalDate localDate = LocalDate.now().plusDays(1L);
        long deadline = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        return this.subscriptionMemberRepository.getMemberCountByIslandId(islandId, deadline);
    }

    /**
     * retrieve the membership count by membership id.
     *
     * @param membershipId membership id.
     * @return member count.
     */
    public Integer getMemberCountByMembershipId(String membershipId) {
        LocalDate localDate = LocalDate.now().plusDays(1L);
        long deadline = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        return this.subscriptionMemberRepository.getMemberCountByMembershipId(membershipId, deadline);
    }

    @Transactional
    public void subscribeMembershipWithWechatOrder(WechatOrder wechatOrder) {
        if (Objects.isNull(wechatOrder) || WechatOrderState.SUCCESS.getValue() != wechatOrder.getState()) {
            return;
        }

        List<Payment> paymentList = this.paymentService.retrievePaymentsByOrderId(wechatOrder.getId());

        if (paymentList.stream().allMatch(payment -> PaymentState.OPEN.getValue() == payment.getState())) {
            return;
        }

        paymentList.stream()
                .filter(payment -> PaymentState.DRAFTED.getValue() == payment.getState())
                .forEach(payment -> payment.setState(PaymentState.OPEN.getValue()));

        SubscribeMembership subscribeMembership = this.subscriptionMemberRepository.findByUserIdAndMembershipIdAndDeletedIsFalse(
                wechatOrder.getUserId(), wechatOrder.getMemberShipSkuId());

        this.paymentService.updateAll(paymentList);
    }

    @Transactional
    public void createSubscriptionMember(WechatOrder wechatOrder) {
        MembershipSku sku = this.membershipSkuService.retrieveMembershipSkuById(wechatOrder.getMemberShipSkuId());




        SubscribeMembership subscribeMembership = SubscribeMembership.builder()
                .id(String.valueOf(this.idGenerator.nextId()))
                .userId(wechatOrder.getUserId())
                .islandId(sku.getIslandId())
                .membershipId(sku.getMembershipId())
                .expireTime(wechatOrder.get)
                .build();

        this.subscriptionMemberRepository.save(subscribeMembership);
    }

}