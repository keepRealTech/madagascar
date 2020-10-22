package com.keepreal.madagascar.asity.consumer;

import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.MessageListener;
import com.google.protobuf.InvalidProtocolBufferException;
import com.keepreal.madagascar.asity.service.RongCloudService;
import com.keepreal.madagascar.common.constants.Templates;
import com.keepreal.madagascar.coua.IslandEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Represents the island event listener.
 */
@Component
@Slf4j
public class IslandEventListener implements MessageListener {

    private final RongCloudService rongCloudService;

    /**
     * Constructs the island event listener.
     *
     * @param rongCloudService {@link RongCloudService}.
     */
    public IslandEventListener(RongCloudService rongCloudService) {
        this.rongCloudService = rongCloudService;
    }

    /**
     * Consumes the island events.
     *
     * @param message {@link Message}.
     * @param context {@link ConsumeContext}.
     * @return {@link Action}.
     */
    @Override
    public Action consume(Message message, ConsumeContext context) {
        try {
            if (Objects.isNull(message) || Objects.isNull(message.getBody())) {
                return Action.CommitMessage;
            }

            IslandEvent islandEvent = IslandEvent.parseFrom(message.getBody());

            switch (islandEvent.getType()) {
                case ISLAND_EVENT_CREATE:
                    this.rongCloudService.sendMessage(new String[]{islandEvent.getCreateIslandEvent().getHostId()}, Templates.ASITY_CREATE_ISLAND_CONTENT_DEFAULT);
                    return Action.CommitMessage;
                case ISLAND_EVENT_NONE:
                case UNRECOGNIZED:
                default:
            }

            return Action.CommitMessage;
        } catch (InvalidProtocolBufferException e) {
            log.warn("Bad formatted island event, skipped.");
            return Action.CommitMessage;
        } catch (Exception e) {
            return Action.ReconsumeLater;
        }
    }

}
