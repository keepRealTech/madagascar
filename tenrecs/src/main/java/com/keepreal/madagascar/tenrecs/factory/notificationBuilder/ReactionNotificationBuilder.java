package com.keepreal.madagascar.tenrecs.factory.notificationBuilder;

import com.keepreal.madagascar.common.FeedMessage;
import com.keepreal.madagascar.common.NotificationType;
import com.keepreal.madagascar.common.ReactionMessage;
import com.keepreal.madagascar.tenrecs.NotificationEvent;
import com.keepreal.madagascar.tenrecs.NotificationEventType;
import com.keepreal.madagascar.tenrecs.model.Feed;
import com.keepreal.madagascar.tenrecs.model.Notification;
import com.keepreal.madagascar.tenrecs.model.Reaction;
import com.keepreal.madagascar.tenrecs.service.NotificationService;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;

/**
 * Implements the {@link NotificationBuilder}.
 */
public class ReactionNotificationBuilder implements NotificationBuilder {

    private NotificationEvent event;
    private NotificationService notificationService;

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
     * Sets the notification service.
     *
     * @param notificationService {@link NotificationService}.
     * @return this.
     */
    public ReactionNotificationBuilder setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
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
                || Objects.isNull(this.notificationService)
                || !this.event.getType().equals(NotificationEventType.NOTIFICATION_EVENT_NEW_REACTION)
                || Objects.isNull(this.event.getReactionEvent())) {
            return null;
        }

        Optional<Notification> lastNotification = this.notificationService.retrieveLastByReactionAuthorIdAndReactionFeedId(
                this.event.getUserId(), this.event.getReactionEvent().getReaction().getFeedId());
        if (lastNotification.isPresent()
                && lastNotification.get().getTimestamp() > LocalDateTime.now().minusDays(1).atZone(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli()) {
            Notification ln = lastNotification.get();
            ln.setTimestamp(this.event.getTimestamp());
            ln.getReaction().getTypes().addAll(this.event.getReactionEvent().getReaction().getReactionTypeList());
            return ln;
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
                .types(new HashSet<>(reactionMessage.getReactionTypeList()))
                .createdAt(reactionMessage.getCreatedAt())
                .build();
    }

}