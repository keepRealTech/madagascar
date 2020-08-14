package com.keepreal.madagascar.lemur.dtoFactory;

import com.keepreal.madagascar.common.UserMessage;
import com.keepreal.madagascar.coua.MembershipMessage;
import com.keepreal.madagascar.vanga.MembershipSkuMessage;
import com.keepreal.madagascar.vanga.UserPaymentMessage;
import org.springframework.stereotype.Component;
import swagger.model.UserPaymentDTO;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.Objects;

/**
 * Represents the payment dto factory.
 */
@Component
public class PaymentDTOFactory {

    private final UserDTOFactory userDTOFactory;
    private final MembershipDTOFactory membershipDTOFactory;
    private final SkuDTOFactory skuDTOFactory;

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

        ZonedDateTime expiration = ZonedDateTime.ofInstant(Instant.ofEpochMilli(userPaymentMessage.getExpiresAt()), ZoneId.systemDefault());
        userPaymentDTO.setExpiration(expiration.with(ChronoField.SECOND_OF_DAY, 0).toInstant().toEpochMilli());

        return userPaymentDTO;
    }

}
