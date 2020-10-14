package com.keepreal.madagascar.lemur.dtoFactory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.keepreal.madagascar.common.PaymentState;
import com.keepreal.madagascar.common.UserMessage;
import com.keepreal.madagascar.common.UserPaymentType;
import com.keepreal.madagascar.common.constants.Constants;
import com.keepreal.madagascar.common.constants.Templates;
import com.keepreal.madagascar.coua.MembershipMessage;
import com.keepreal.madagascar.vanga.MembershipSkuMessage;
import com.keepreal.madagascar.vanga.UserPaymentMessage;
import com.keepreal.madagascar.vanga.UserWithdrawMessage;
import org.springframework.stereotype.Component;
import swagger.model.BriefUserDTO;
import swagger.model.PaymentType;
import swagger.model.UserPaymentDTO;
import swagger.model.UserPaymentDTOV11;
import swagger.model.UserWithdrawDTO;
import swagger.model.WithdrawState;

import javax.validation.Valid;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents the payment dto factory.
 */
@Component
public class PaymentDTOFactory {

    private final UserDTOFactory userDTOFactory;
    private final MembershipDTOFactory membershipDTOFactory;
    private final SkuDTOFactory skuDTOFactory;

    private static final long PERMANENT_TIMESTAMP = 4070880000000L; // 2099-01-01 00:00:00

    /**
     * Constructs the {@link PaymentDTOFactory}.
     *
     * @param userDTOFactory       {@link UserDTOFactory}.
     * @param membershipDTOFactory {@link MembershipDTOFactory}.
     * @param skuDTOFactory        {@link SkuDTOFactory}.
     */
    public PaymentDTOFactory(UserDTOFactory userDTOFactory,
                             MembershipDTOFactory membershipDTOFactory,
                             SkuDTOFactory skuDTOFactory) {
        this.userDTOFactory = userDTOFactory;
        this.membershipDTOFactory = membershipDTOFactory;
        this.skuDTOFactory = skuDTOFactory;
    }

    /**
     * Builds the {@link UserPaymentDTO}.
     *
     * @param userPaymentMessage   {@link UserPaymentMessage}.
     * @param userMessage          {@link UserMessage}.
     * @param membershipSkuMessage {@link MembershipSkuMessage}.
     * @param membershipMessage    {@link MembershipMessage}.
     * @return {@link UserPaymentDTO}.
     */
    public UserPaymentDTO valueOf(UserPaymentMessage userPaymentMessage,
                                  UserMessage userMessage,
                                  MembershipSkuMessage membershipSkuMessage,
                                  MembershipMessage membershipMessage) {
        if (Objects.isNull(userPaymentMessage)) {
            return null;
        }

        UserPaymentDTO userPaymentDTO = new UserPaymentDTO();
        userPaymentDTO.setId(userPaymentMessage.getId());
        userPaymentDTO.setHost(this.userDTOFactory.briefValueOf(userMessage));
        userPaymentDTO.setMembership(this.membershipDTOFactory.briefValueOf(membershipMessage));
        userPaymentDTO.setMembershipSku(this.skuDTOFactory.valueOf(membershipSkuMessage));
        userPaymentDTO.setCreatedAt(userPaymentMessage.getCreatedAt());
        userPaymentDTO.setPriceInCents(userPaymentMessage.getPriceInCents());

        ZonedDateTime expiration = ZonedDateTime.ofInstant(Instant.ofEpochMilli(userPaymentMessage.getExpiresAt()), ZoneId.systemDefault());
        userPaymentDTO.setExpiration(expiration.with(ChronoField.SECOND_OF_DAY, 0).toInstant().toEpochMilli());

        /*
            当会员从永久改为按月付费之后，如果该用户买过永久会员，订单有效期仍然是永久有效
         */
        if (userPaymentDTO.getExpiration() >= PERMANENT_TIMESTAMP) {
            userPaymentDTO.getMembershipSku().setIsPermanent(true);
        }

        return userPaymentDTO;
    }

    /**
     * Builds the {@link UserPaymentDTO}.
     *
     * @param userPaymentMessage   {@link UserPaymentMessage}.
     * @param userMessage          {@link UserMessage}.
     * @param membershipSkuMessage {@link MembershipSkuMessage}.
     * @param membershipMessage    {@link MembershipMessage}.
     * @return {@link UserPaymentDTO}.
     */
    public UserPaymentDTOV11 v11ValueOf(UserPaymentMessage userPaymentMessage,
                                     UserMessage userMessage,
                                     MembershipSkuMessage membershipSkuMessage,
                                     MembershipMessage membershipMessage) {
        if (Objects.isNull(userPaymentMessage)) {
            return null;
        }

        UserPaymentDTOV11 userPaymentDTO = new UserPaymentDTOV11();
        userPaymentDTO.setId(userPaymentMessage.getId());
        userPaymentDTO.setHost(this.userDTOFactory.briefValueOf(userMessage));
        userPaymentDTO.setCreatedAt(userPaymentMessage.getCreatedAt());
        userPaymentDTO.setPriceInCents(userPaymentMessage.getPriceInCents());

        if (UserPaymentType.PAYMENT_TYPE_MEMBERSHIP.equals(userPaymentMessage.getType())) {
            userPaymentDTO.setType(PaymentType.MEMBERSHIP);
            userPaymentDTO.setPrivileges(Arrays.asList(membershipMessage.getDescription().split(",")));
            userPaymentDTO.setTimeInMonths(membershipSkuMessage.getTimeInMonths());
            userPaymentDTO.setName(membershipMessage.getName());

            ZonedDateTime expiration = ZonedDateTime.ofInstant(Instant.ofEpochMilli(userPaymentMessage.getExpiresAt()), ZoneId.systemDefault());
            userPaymentDTO.setExpiration(expiration.with(ChronoField.SECOND_OF_DAY, 0).toInstant().toEpochMilli());

            if (userPaymentDTO.getExpiration() >= PERMANENT_TIMESTAMP) {
                userPaymentDTO.setIsPermanent(true);
            }
        } else if (UserPaymentType.PAYMENT_TYPE_FEED.equals(userPaymentMessage.getType())) {
            userPaymentDTO.setType(PaymentType.FEED);
            userPaymentDTO.setName(Templates.PAYMENT_TYPE_FEED);
            userPaymentDTO.setPrivileges(Collections.singletonList(String.format(Templates.PAYMENT_FEED_PRIVILEGE, userPaymentDTO.getPriceInCents() / 100L)));
        }

        return null;
    }

    /**
     * Converts {@link UserWithdrawMessage} into {@link UserWithdrawDTO}.
     *
     * @param userWithdrawMessage {@link UserWithdrawMessage}.
     * @return {@link UserWithdrawDTO}.
     */
    public UserWithdrawDTO valueOf(UserWithdrawMessage userWithdrawMessage) {
        if (Objects.isNull(userWithdrawMessage)) {
            return null;
        }

        UserWithdrawDTO userWithdrawDTO = new UserWithdrawDTO();
        userWithdrawDTO.setId(userWithdrawMessage.getId());
        userWithdrawDTO.setAmountInCents(userWithdrawMessage.getAmountInCents());
        userWithdrawDTO.setCreatedAt(userWithdrawMessage.getCreatedAt());
        userWithdrawDTO.setState(this.convertState(userWithdrawMessage.getState()));
        return userWithdrawDTO;
    }

    /**
     * Converts the state into swagger model.
     *
     * @param state {@link PaymentState}.
     * @return {@link WithdrawState}.
     */
    private WithdrawState convertState(PaymentState state) {
        if (Objects.isNull(state)) {
            return WithdrawState.PROCESSING;
        }

        switch (state) {
            case PAYMENT_STATE_CLOSED:
                return WithdrawState.SETTLED;
            case PAYMENT_STATE_OPEN:
            case UNRECOGNIZED:
            default:
                return WithdrawState.PROCESSING;
        }
    }

}
