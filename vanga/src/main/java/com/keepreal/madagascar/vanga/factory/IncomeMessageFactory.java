package com.keepreal.madagascar.vanga.factory;

import com.keepreal.madagascar.coua.MembershipMessage;
import com.keepreal.madagascar.vanga.FeedChargeMessage;
import com.keepreal.madagascar.vanga.IncomeDetailMessage;
import com.keepreal.madagascar.vanga.IncomeMonthlyMessage;
import com.keepreal.madagascar.vanga.IncomeProfileMessage;
import com.keepreal.madagascar.vanga.SponsorMessage;
import com.keepreal.madagascar.vanga.SupportListMessage;
import com.keepreal.madagascar.vanga.SupportMembershipMessage;
import com.keepreal.madagascar.vanga.model.IncomeDetail;
import com.keepreal.madagascar.vanga.model.IncomeProfile;
import com.keepreal.madagascar.vanga.model.IncomeSupport;
import com.keepreal.madagascar.vanga.model.MembershipSku;
import com.keepreal.madagascar.vanga.model.Payment;
import com.keepreal.madagascar.vanga.model.PaymentType;
import com.keepreal.madagascar.vanga.repository.FeedChargeRepository;
import com.keepreal.madagascar.vanga.repository.PaymentRepository;
import com.keepreal.madagascar.vanga.service.SkuService;
import com.keepreal.madagascar.vanga.util.DateUtils;
import org.springframework.stereotype.Component;

import com.keepreal.madagascar.common.constants.Templates;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class IncomeMessageFactory {

    private final SkuService skuService;
    private final PaymentRepository paymentRepository;
    private final FeedChargeRepository feedChargeRepository;

    public IncomeMessageFactory(SkuService skuService,
                                PaymentRepository paymentRepository,
                                FeedChargeRepository feedChargeRepository) {
        this.skuService = skuService;
        this.paymentRepository = paymentRepository;
        this.feedChargeRepository = feedChargeRepository;
    }

    public IncomeProfileMessage profileValueOf(String userId, IncomeProfile incomeProfile) {
        Long currentMonthIncome = this.paymentRepository.countAmountByPayeeIdAndValidAfter(userId, DateUtils.startOfMonthTimestamp(1), DateUtils.endOfMonthTimestamp(1));
        Long nextMonthIncome = this.paymentRepository.countAmountByPayeeIdAndValidAfter(userId, DateUtils.startOfMonthTimestamp(2), DateUtils.endOfMonthTimestamp(2));
        Integer currentMonthSupportCount = this.paymentRepository.countSupportCountByPayeeIdAndValidAfter(userId, DateUtils.startOfMonthTimestamp(1), DateUtils.endOfMonthTimestamp(1));

        return IncomeProfileMessage.newBuilder()
                .setTotalIncome(incomeProfile.getAmountInCents())
                .setTotalSupportCount(incomeProfile.getSupportCountReal())
                .setTotalSubscriber(incomeProfile.getMemberCount())
                .setCurrentMonthIncome(currentMonthIncome)
                .setCurrentMonthSupportCount(currentMonthSupportCount)
                .setNextMonthIncome(nextMonthIncome)
                .build();
    }

    public IncomeMonthlyMessage valueOf(IncomeDetail incomeDetail) {
        return IncomeMonthlyMessage.newBuilder()
                .setMonthTimestamp(incomeDetail.getMonthTimestamp())
                .setCurrentMonthIncome(incomeDetail.getAmountInCents())
                .setSupportCount(incomeDetail.getSupportCount())
                .build();
    }

    public SupportListMessage valueOf(IncomeSupport incomeSupport) {
        return SupportListMessage.newBuilder()
                .setUserId(incomeSupport.getSupporterId())
                .setAmountInCents(incomeSupport.getCents())
                .build();
    }

    public SupportMembershipMessage valueOf(MembershipMessage membershipMessage) {
        List<MembershipSku> membershipSkus = this.skuService.retrieveMembershipSkusByMembershipId(membershipMessage.getId());

        Integer supportCount = this.paymentRepository.countSupportCountByPayeeIdAndTimestampAndMemberhipSku(
                membershipMessage.getHostId(),
                DateUtils.startOfMonthTimestamp(),
                DateUtils.endOfMonthTimestamp(),
                membershipSkus.stream().map(MembershipSku::getId).collect(Collectors.toList()));

        Long income = this.paymentRepository.countAmountByPayeeIdAndTimestampAndMemberhipSku(
                membershipMessage.getHostId(),
                DateUtils.startOfMonthTimestamp(),
                DateUtils.endOfMonthTimestamp(),
                membershipSkus.stream().map(MembershipSku::getId).collect(Collectors.toList()));

        return SupportMembershipMessage.newBuilder()
                .setMembershipId(membershipMessage.getId())
                .setMembershipName(membershipMessage.getName())
                .setPriceInMonth(membershipMessage.getPricePerMonth())
                .setIsPermanent(membershipMessage.getPermanent())
                .setIncome(income)
                .setSupportCount(supportCount)
                .build();
    }

    public SponsorMessage sponsorValueOf(String userId) {
        Integer supportCount = this.paymentRepository.countSponsorByPayeeIdAndTimestamp(userId, DateUtils.startOfMonthTimestamp(), DateUtils.endOfMonthTimestamp());
        Long income = this.paymentRepository.countSponsorIncomeByPayeeIdAndTimestamp(userId, DateUtils.startOfMonthTimestamp(), DateUtils.endOfMonthTimestamp());
        return SponsorMessage.newBuilder()
                .setIncome(income)
                .setSupportCount(supportCount)
                .build();
    }

    public FeedChargeMessage feedChargeValueOf(String userId) {
        Integer supportCount = this.feedChargeRepository.countByHostIdAndTimestamp(userId, DateUtils.startOfMonthTimestamp(), DateUtils.endOfMonthTimestamp());
        Long income = this.feedChargeRepository.countAmountByHostIdAndTimestamp(userId, DateUtils.startOfMonthTimestamp(), DateUtils.endOfMonthTimestamp());
        return FeedChargeMessage.newBuilder()
                .setIncome(income)
                .setSupportCount(supportCount)
                .build();
    }

    public IncomeDetailMessage valueOf(Payment payment) {
        String content;
        Long amountInCent;

        if (payment.getMembershipSkuId() != null) {
            MembershipSku membershipSku = this.skuService.retrieveMembershipSkuById(payment.getMembershipSkuId());
            if (membershipSku.getPermanent()) {
                content = String.format(Templates.INCOME_MEMBERSHIP_PERMANENT_CONTENT, membershipSku.getMembershipName());
                amountInCent = membershipSku.getPriceInCents();
            } else {
                content = String.format(Templates.INCOME_MEMBERSHIP_CONTENT, membershipSku.getTimeInMonths(), membershipSku.getMembershipName());
                amountInCent = membershipSku.getTimeInMonths() * membershipSku.getPriceInCents();
            }
        } else if (payment.getType() == PaymentType.SUPPORT.getValue()) {
            content = Templates.INCOME_SPONSOR_CONTENT;
            amountInCent = payment.getAmountInCents();
        } else if (payment.getType() == PaymentType.WECHATPAY.getValue() || payment.getType() == PaymentType.ALIPAY.getValue()) {
            content = Templates.INCOME_FEED_CHARGE_CONTENT;
            amountInCent = payment.getAmountInCents();
        } else {
            return null;
        }

        return IncomeDetailMessage.newBuilder()
                .setUserId(payment.getUserId())
                .setContent(content)
                .setAmountInCents(amountInCent)
                .setTimestamp(payment.getCreatedTime())
                .build();
    }
}