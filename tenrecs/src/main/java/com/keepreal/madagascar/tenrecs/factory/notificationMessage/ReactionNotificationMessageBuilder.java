package com.keepreal.madagascar.tenrecs.factory.notificationMessage;

import com.keepreal.madagascar.common.FeedMessage;
import com.keepreal.madagascar.common.NotificationType;
import com.keepreal.madagascar.common.ReactionMessage;
import com.keepreal.madagascar.tenrecs.NotificationMessage;
import com.keepreal.madagascar.tenrecs.ReactionNotificationMessage;
import com.keepreal.madagascar.tenrecs.model.Feed;
import com.keepreal.madagascar.tenrecs.model.Notification;
import com.keepreal.madagascar.tenrecs.model.Reaction;

import java.util.Objects;

/**
 * Implements the {@link NotificationMessageBuilder}.
 */
public class ReactionNotificationMessageBuilder implements NotificationMessageBuilder {

    private long lastReadTimestamp;
    private Notification notification;

    /**
     * Sets the last read timestamp.
     *
     * @param lastReadTimestamp Last read reaction notification timestamp.
     * @return this.
     */
    public ReactionNotificationMessageBuilder setLastReadTimestamp(long lastReadTimestamp) {
        this.lastReadTimestamp = lastReadTimestamp;
        return this;
    }

    /**
     * Sets the notificaton.
     *
     * @param notification {@link Notification}.
     * @return this.
     */
    public ReactionNotificationMessageBuilder setNotification(Notification notification) {
        this.notification = notification;
        return this;
    }

    /**
     * Builds the {@link NotificationMessage}.
     *
     * @return {@link NotificationMessage}.
     */
    @Override
    public NotificationMessage build() {
        if (Objects.isNull(this.notification)
                || !notification.getType().equals(NotificationType.NOTIFICATION_REACTIONS)) {
            return null;
        }

        ReactionNotificationMessage reactionNotificationMessage = ReactionNotificationMessage.newBuilder()
                .setFeed(this.toFeedMessage(this.notification.getFeed()))
                .setReaction(this.toReactionMessage(this.notification.getReaction()))
                .build();

        return NotificationMessage.newBuilder()
                .setId(String.valueOf(this.notification.getId()))
                .setType(NotificationType.NOTIFICATION_REACTIONS)
                .setUserId(this.notification.getUserId())
                .setHasRead(this.notification.getCreatedAt().compareTo(this.lastReadTimestamp) < 0)
                .setReactionNotification(reactionNotificationMessage)
                .setCreatedAt(this.notification.getCreatedAt())
                .build();
    }

    /**
     * Converts {@link Feed} into {@link FeedMessage}.
     *
     * @param feed {@link Feed}.
     * @return {@link FeedMessage}.
     */
    private FeedMessage toFeedMessage(Feed feed) {
        if (Objects.isNull(feed)) {
            return null;
        }

        return FeedMessage.newBuilder()
                .setId(feed.getId())
                .setUserId(feed.getAuthorId())
                .setText(feed.getText())
                .setIslandId(feed.getIslandId())
                .addAllImageUris(feed.getImageUris())
                .setFromHost(feed.getFromHost())
                .setCreatedAt(feed.getCreatedAt())
                .setCommentsCount(0)
                .setLikesCount(0)
                .setRepostCount(0)
                .build();
    }

    /**
     * Converts {@link Reaction} into {@link ReactionMessage}.
     *
     * @param reaction {@link Reaction}.
     * @return {@link ReactionMessage}.
     */
    private ReactionMessage toReactionMessage(Reaction reaction) {
        if (Objects.isNull(reaction)) {
            return null;
        }

        ReactionMessage.Builder reactionMessageBuilder = ReactionMessage.newBuilder()
                .setId(reaction.getId())
                .setFeedId(reaction.getFeedId())
                .setUserId(reaction.getAuthorId())
                .addAllReactionType(reaction.getTypes())
                .setCreatedAt(reaction.getCreatedAt());

        return reactionMessageBuilder.build();
    }

}