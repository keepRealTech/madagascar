package com.keepreal.madagascar.tenrecs.factory.notificationBuilder;

import com.keepreal.madagascar.common.FeedMessage;
import com.keepreal.madagascar.common.NotificationType;
import com.keepreal.madagascar.common.ReactionMessage;
import com.keepreal.madagascar.tenrecs.NotificationEvent;
import com.keepreal.madagascar.tenrecs.NotificationEventType;
import com.keepreal.madagascar.tenrecs.model.Feed;
import com.keepreal.madagascar.tenrecs.model.Notification;
import com.keepreal.madagascar.tenrecs.model.Reaction;

import java.util.Objects;

/**
 * Implements the {@link NotificationBuilder}.
 */
public class ReactionNotificationBuilder implements NotificationBuilder {

    private NotificationEvent event;

    /**
     * Sets the notificaton event.
     *
     * @param event {@link NotificationEvent}.
     * @return this.
     */
    @Override
    public ReactionNotificationBuilder setEvent(NotificationEvent event) {
        this.event = event;
        return this;
    }

    /**
     * Builds the {@link Notification}.
     *
     * @return {@link Notification}.
     */
    @Override
    public Notification build() {
        if (Objects.isNull(this.event)
                || !this.event.getType().equals(NotificationEventType.NOTIFICATION_EVENT_NEW_REACTION)
                || Objects.isNull(this.event.getReactionEvent())) {
            return null;
        }

        return Notification.builder()
                .type(NotificationType.NOTIFICATION_REACTIONS)
                .userId(this.event.getUserId())
                .eventId(this.event.getEventId())
                .timestamp(this.event.getTimestamp())
                .feed(this.toFeed(this.event.getReactionEvent().getFeed()))
                .reaction(this.toReaction(this.event.getReactionEvent().getReaction()))
                .build();
    }

    /**
     * Converts {@link FeedMessage} into {@link Feed}.
     *
     * @param feedMessage {@link FeedMessage}.
     * @return {@link Feed}.
     */
    private Feed toFeed(FeedMessage feedMessage) {
        if (Objects.isNull(feedMessage)) {
            return null;
        }

        return Feed.builder()
                .id(feedMessage.getId())
                .islandId(feedMessage.getIslandId())
                .authorId(feedMessage.getUserId())
                .text(feedMessage.getText())
                .imageUris(feedMessage.getImageUrisList())
                .createdAt(feedMessage.getCreatedAt())
                .build();
    }

    /**
     * Converts {@link ReactionMessage} into {@link Reaction}.
     *
     * @param reactionMessage {@link ReactionMessage}.
     * @return {@link Reaction}.
     */
    private Reaction toReaction(ReactionMessage reactionMessage) {
        if (Objects.isNull(reactionMessage)) {
            return null;
        }

        return Reaction.builder()
                .id(reactionMessage.getId())
                .authorId(reactionMessage.getUserId())
                .feedId(reactionMessage.getFeedId())
                .types(reactionMessage.getReactionTypeList())
                .createdAt(reactionMessage.getCreatedAt())
                .build();
    }

}