package com.keepreal.madagascar.asity.consumer;

import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.MessageListener;
import com.google.protobuf.InvalidProtocolBufferException;
import com.keepreal.madagascar.asity.service.RongCloudService;
import com.keepreal.madagascar.coua.IslandEvent;
import com.keepreal.madagascar.coua.IslandEventType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@Slf4j
public class IslandEventListener implements MessageListener {

    private final RongCloudService rongCloudService;

    public IslandEventListener(RongCloudService rongCloudService) {
        this.rongCloudService = rongCloudService;
    }

    @Override
    public Action consume(Message message, ConsumeContext context) {
        try {
            if (Objects.isNull(message) || Objects.isNull(message.getBody())) {
                return Action.CommitMessage;
            }
            IslandEvent islandEvent = IslandEvent.parseFrom(message.getBody());

            if (IslandEventType.ISLAND_EVENT_CREATE.equals(islandEvent.getType())) {
                this.rongCloudService.sentCreateIslandNotice(islandEvent.getCreateIslandEvent());
                return Action.CommitMessage;
            }

            return Action.CommitMessage;
        } catch (InvalidProtocolBufferException e) {
            log.warn("Bad formatted notification event, skipped.");
            return Action.CommitMessage;
        } catch (Exception e) {
            return Action.ReconsumeLater;
        }
    }
}
