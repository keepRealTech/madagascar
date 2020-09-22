package com.keepreal.madagascar.coua.consumer;

import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.MessageListener;
import com.google.protobuf.InvalidProtocolBufferException;
import com.keepreal.madagascar.coua.service.MembershipService;
import com.keepreal.madagascar.tenrecs.NotificationEvent;
import com.keepreal.madagascar.tenrecs.NotificationEventType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Represents the logic consuming {@link NotificationEvent}.
 */
@Component
@Slf4j
public class NotificationEventListener implements MessageListener {

    private final MembershipService membershipService;

    public NotificationEventListener(MembershipService membershipService) {
        this.membershipService = membershipService;
    }

    /**
     * Implements the logic after consumption.
     *
     * @param message Message payload.
     * @param context Context.
     * @return {@link Action}.
     */
    @Override
    public Action consume(Message message, ConsumeContext context) {
        try {
            NotificationEvent event = NotificationEvent.parseFrom(message.getBody());

            if (Objects.isNull(event)) {
                return Action.CommitMessage;
            }

            if (NotificationEventType.NOTIFICATION_EVENT_NEW_MEMBER.equals(event.getType())) {
                String membershipId = event.getMemberEvent().getMembershipId();
                this.membershipService.addMemberCount(membershipId);
            }

            return Action.CommitMessage;
        } catch (DuplicateKeyException exception) {
            log.warn("Duplicated consumption, skipped.");
            return Action.CommitMessage;
        } catch (InvalidProtocolBufferException e) {
            log.warn("Bad formatted notification event, skipped.");
            return Action.CommitMessage;
        } catch (Exception e) {
            return Action.ReconsumeLater;
        }
    }

}