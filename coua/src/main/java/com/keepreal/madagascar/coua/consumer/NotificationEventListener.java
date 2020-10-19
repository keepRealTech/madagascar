package com.keepreal.madagascar.coua.consumer;

import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.MessageListener;
import com.google.protobuf.InvalidProtocolBufferException;
import com.keepreal.madagascar.coua.service.IslandInfoService;
import com.keepreal.madagascar.coua.service.MembershipService;
import com.keepreal.madagascar.tenrecs.NotificationEvent;
import com.keepreal.madagascar.tenrecs.NotificationEventType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Objects;

/**
 * Represents the logic consuming {@link NotificationEvent}.
 */
@Component
@Slf4j
public class NotificationEventListener implements MessageListener {

    private final MembershipService membershipService;
    private final IslandInfoService islandInfoService;

    public NotificationEventListener(MembershipService membershipService,
                                     IslandInfoService islandInfoService) {
        this.membershipService = membershipService;
        this.islandInfoService = islandInfoService;
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

            if (NotificationEventType.NOTIFICATION_EVENT_NEW_BALANCE.equals(event.getType())) {
                if (Objects.isNull(event.getBalanceEvent())
                        || StringUtils.isEmpty(event.getBalanceEvent().getHostId())) {
                    log.error("balanceEvent doesn't has host id , eventId is {}", event.getEventId());
                    return Action.CommitMessage;
                }
                this.islandInfoService.updateSupportTargetIfExisted(event.getBalanceEvent().getHostId(),
                        event.getBalanceEvent().getAmountInCents());
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