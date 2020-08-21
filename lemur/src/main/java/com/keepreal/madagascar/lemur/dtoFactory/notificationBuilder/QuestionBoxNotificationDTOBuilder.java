package com.keepreal.madagascar.lemur.dtoFactory.notificationBuilder;

import com.keepreal.madagascar.common.FeedMessage;
import com.keepreal.madagascar.lemur.service.FeedService;
import com.keepreal.madagascar.tenrecs.NoticeNotificationMessage;
import com.keepreal.madagascar.tenrecs.NotificationMessage;
import swagger.model.NoticeDTO;
import swagger.model.NoticeType;
import swagger.model.NotificationDTO;
import swagger.model.NotificationType;
import swagger.model.SimpleQuestionBoxDTO;

import java.util.Objects;

/**
 * Represents the question box notice notification dto builder.
 */
public class QuestionBoxNotificationDTOBuilder implements NotificationDTOBuilder {
    private NotificationMessage notificationMessage;
    private FeedService feedService;

    /**
     * Sets the notification message.
     *
     * @param notificationMessage {@link NotificationMessage}.
     * @return {@link NoticeNotificationDTOBuilder}.
     */
    @Override
    public QuestionBoxNotificationDTOBuilder setNotificationMessage(NotificationMessage notificationMessage) {
        this.notificationMessage = notificationMessage;
        return this;
    }

    /**
     * Sets the {@link FeedService}.
     *
     * @param feedService {@link FeedService}.
     * @return {@link NoticeNotificationDTOBuilder}.
     */
    public QuestionBoxNotificationDTOBuilder setFeedService(FeedService feedService) {
        this.feedService = feedService;
        return this;
    }

    /**
     * Builds the {@link NotificationDTO}.
     *
     * @return {@link NotificationDTO}.
     */
    @Override
    public NotificationDTO build() {
        if (Objects.isNull(this.notificationMessage)) {
            return null;
        }

        NotificationDTO notificationDTO = new NotificationDTO();
        notificationDTO.setId(this.notificationMessage.getId());
        notificationDTO.setHasRead(this.notificationMessage.getHasRead());
        notificationDTO.setNotificationType(NotificationType.BOX_NOTICE);
        notificationDTO.setCreatedAt(this.notificationMessage.getTimestamp());
        notificationDTO.setQuestion(this.valueOf(this.notificationMessage.getNoticeNotification()));

        return notificationDTO;
    }

    /**
     * Converts the {@link NoticeNotificationMessage} into {@link NoticeDTO}.
     *
     * @param message {@link NoticeNotificationMessage}.
     * @return {@link NoticeDTO}.
     */
    private SimpleQuestionBoxDTO valueOf(NoticeNotificationMessage message) {
        if (Objects.isNull(message)) {
            return null;
        }

        FeedMessage feedMessage = this.feedService.retrieveFeedById(message.getNewQuestionNotice().getFeedId()
                , message.getNewQuestionNotice().getAuthorId());

        SimpleQuestionBoxDTO questionDTO = new SimpleQuestionBoxDTO();

        switch (message.getType()) {
            case NOTICE_TYPE_BOX_NEW_QUESTION:
                if (Objects.isNull(message.getNewQuestionNotice())) {
                    return questionDTO;
                }
                questionDTO.setNoticeType(NoticeType.BOX_NOTICE_NEW_QUESTION);
                questionDTO.setFeedId(message.getNewQuestionNotice().getFeedId());
                questionDTO.setText(feedMessage.getText());
                questionDTO.setPrice(feedMessage.getQuestion().getPriceInCents().getValue());
                return questionDTO;
            case NOTICE_TYPE_BOX_NEW_ANSWER:
                if (Objects.isNull(message.getNewAnswerNotice())) {
                    return questionDTO;
                }
                questionDTO.setNoticeType(NoticeType.BOX_NOTICE_NEW_ANSWER);
                questionDTO.setFeedId(message.getNewAnswerNotice().getFeedId());
                questionDTO.setText(feedMessage.getText());
                return questionDTO;
            default:
        }
        return questionDTO;
    }
}
