package com.keepreal.madagascar.lemur.dtoFactory;

import com.keepreal.madagascar.common.FeedMessage;
import com.keepreal.madagascar.fossa.CheckNewFeedsMessage;
import com.keepreal.madagascar.lemur.service.UserService;
import org.springframework.stereotype.Component;
import swagger.model.BriefFeedDTO;
import swagger.model.CheckFeedsDTO;
import swagger.model.FeedDTO;

import java.util.Objects;

/**
 * Represents the feed dto factory.
 */
@Component
public class FeedDTOFactory {

    private final UserService userService;
    private final UserDTOFactory userDTOFactory;

    /**
     * Constructs the feed dto factory.
     *
     * @param userService    {@link UserService}.
     * @param userDTOFactory {@link UserDTOFactory}.
     */
    public FeedDTOFactory(UserService userService, UserDTOFactory userDTOFactory) {
        this.userService = userService;
        this.userDTOFactory = userDTOFactory;
    }

    /**
     * Converts the {@link FeedMessage} into {@link FeedDTO}.
     *
     * @param feed {@link FeedMessage}.
     * @return {@link FeedDTO}.
     */
    public FeedDTO valueOf(FeedMessage feed) {
        if (Objects.isNull(feed)) {
            return null;
        }

        FeedDTO feedDTO = new FeedDTO();
        feedDTO.setId(feed.getId());
        feedDTO.setIslandId(feed.getIslandId());
        feedDTO.setText(feed.getText());
        feedDTO.setImagesUris(feed.getImageUrisList());
        feedDTO.setFromHost(feed.getFromHost());
        feedDTO.setLikesCount(feedDTO.getLikesCount());
        feedDTO.setCommentsCount(feed.getCommentsCount());
        feedDTO.setRepostCount(feed.getRepostCount());
        feedDTO.setCreatedAt(feed.getCreatedAt());

        feedDTO.setUser(this.userDTOFactory.briefValueOf(
                this.userService.retrieveUserById(feed.getUserId())));

        return feedDTO;
    }

    /**
     * Converts the {@link FeedMessage} into {@link BriefFeedDTO}.
     *
     * @param feed {@link FeedMessage}.
     * @return {@link BriefFeedDTO}.
     */
    public BriefFeedDTO briefValueOf(FeedMessage feed) {
        if (Objects.isNull(feed)) {
            return null;
        }

        BriefFeedDTO briefFeedDTO = new BriefFeedDTO();
        briefFeedDTO.setId(feed.getId());
        briefFeedDTO.setIslandId(feed.getIslandId());
        briefFeedDTO.setText(feed.getText());
        briefFeedDTO.setImagesUris(feed.getImageUrisList());
        briefFeedDTO.setFromHost(feed.getFromHost());
        briefFeedDTO.setCreatedAt(feed.getCreatedAt());

        briefFeedDTO.setUser(this.userDTOFactory.briefValueOf(
                this.userService.retrieveUserById(feed.getUserId())));

        return briefFeedDTO;
    }

    /**
     * Converts {@link CheckNewFeedsMessage} into {@link CheckFeedsDTO}.
     *
     * @param checkNewFeeds {@link CheckNewFeedsMessage}.
     * @return {@link CheckFeedsDTO}.
     */
    public CheckFeedsDTO valueOf(CheckNewFeedsMessage checkNewFeeds) {
        if (Objects.isNull(checkNewFeeds)) {
            return null;
        }

        CheckFeedsDTO checkFeedsDTO = new CheckFeedsDTO();
        checkFeedsDTO.setIslandId(checkFeedsDTO.getIslandId());
        checkFeedsDTO.setHasNew(checkNewFeeds.getHasNewFeeds());

        return checkFeedsDTO;
    }

}
