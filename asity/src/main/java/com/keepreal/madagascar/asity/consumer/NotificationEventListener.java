package com.keepreal.madagascar.asity.consumer;

import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.MessageListener;
import com.aliyun.openservices.ons.api.order.ConsumeOrderContext;
import com.keepreal.madagascar.asity.service.ChatgroupService;
import com.keepreal.madagascar.asity.service.MembershipService;
import com.keepreal.madagascar.asity.service.RongCloudService;
import com.keepreal.madagascar.coua.MembershipMessage;
import com.keepreal.madagascar.tenrecs.NotificationEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Objects;

/**
 * Represents the notification event listener.
 */
@Component
@Slf4j
public class NotificationEventListener implements MessageListener {

    private final ChatgroupService chatgroupService;
    private final MembershipService membershipService;
    private final RongCloudService rongCloudService;

    /**
     * Constructs the notification event listener.
     *
     * @param chatgroupService  {@link ChatgroupService}.
     * @param membershipService {@link MembershipService}.
     * @param rongCloudService  {@link RongCloudService}.
     */
    public NotificationEventListener(ChatgroupService chatgroupService,
                                     MembershipService membershipService,
                                     RongCloudService rongCloudService) {
        this.chatgroupService = chatgroupService;
        this.membershipService = membershipService;
        this.rongCloudService = rongCloudService;
    }

    /**
     * Implements the listener consume function logic.
     *
     * @param message {@link Message} Message queue message.
     * @param context {@link ConsumeOrderContext} Consume context if applicable.
     * @return {@link Action}.
     */
    @Override
    public Action consume(Message message, ConsumeContext context) {
        log.info("starting consuming a notification message.");
        try {
            NotificationEvent event = NotificationEvent.parseFrom(message.getBody());

            if (Objects.isNull(event)) {
                return Action.CommitMessage;
            }

            switch (event.getType()) {
                case NOTIFICATION_EVENT_NEW_UNSUBSCRIBE:
                    log.info("dealing new unsubscription.");
                    if (Objects.isNull(event.getUnsubscribeEvent())
                            || StringUtils.isEmpty(event.getUnsubscribeEvent().getSubscriberId())) {
                        break;
                    }
                    this.chatgroupService.quitChatgroupsByIslandId(event.getUnsubscribeEvent().getIslandId(),
                            event.getUnsubscribeEvent().getSubscriberId());
                    break;
                case NOTIFICATION_EVENT_NEW_MEMBER:
                    log.info("dealing with new membership subscription.");
                    if (Objects.isNull(event.getMemberEvent())
                            || StringUtils.isEmpty(event.getMemberEvent().getMemberId())
                            || StringUtils.isEmpty(event.getUserId())) {
                        break;
                    }

                    MembershipMessage membership = this.membershipService.retrieveMembershipById(event.getMemberEvent().getMembershipId());

                    this.rongCloudService.sendThanks(event, membership);
                    break;
                case NOTIFICATION_EVENT_NEW_SUBSCRIBE:
                default:
            }

            return Action.CommitMessage;
        } catch (Exception e) {
            log.error("Failed to commit new subscription event.");
            return Action.ReconsumeLater;
        }
    }

}