package com.keepreal.madagascar.lemur.dtoFactory;

import com.keepreal.madagascar.common.FeedMessage;
import com.keepreal.madagascar.common.IslandMessage;
import com.keepreal.madagascar.common.UserMessage;
import com.keepreal.madagascar.coua.CheckNewFeedsMessage;
import com.keepreal.madagascar.lemur.service.IslandService;
import com.keepreal.madagascar.lemur.service.UserService;
import org.springframework.stereotype.Component;
import swagger.model.BriefFeedDTO;
import swagger.model.CheckFeedsDTO;
import swagger.model.FeedDTO;

import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents the feed dto factory.
 */
@Component
public class FeedDTOFactory {

    private final IslandService islandService;
    private final IslandDTOFactory islandDTOFactory;
    private final UserService userService;
    private final UserDTOFactory userDTOFactory;
    private final CommentDTOFactory commentDTOFactory;

    /**
     * Constructs the feed dto factory.
     *
     * @param islandService     {@link IslandService}.
     * @param islandDTOFactory  {@link IslandDTOFactory}.
     * @param userService       {@link UserService}.
     * @param userDTOFactory    {@link UserDTOFactory}.
     * @param commentDTOFactory {@link CommentDTOFactory}.
     */
    public FeedDTOFactory(IslandService islandService,
                          IslandDTOFactory islandDTOFactory,
                          UserService userService,
                          UserDTOFactory userDTOFactory,
                          CommentDTOFactory commentDTOFactory) {
        this.islandService = islandService;
        this.islandDTOFactory = islandDTOFactory;
        this.userService = userService;
        this.userDTOFactory = userDTOFactory;
        this.commentDTOFactory = commentDTOFactory;
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

        IslandMessage islandMessage = this.islandService.retrieveIslandById(feed.getIslandId());
        UserMessage userMessage = this.userService.retrieveUserById(feed.getUserId());

        FeedDTO feedDTO = new FeedDTO();
        feedDTO.setId(feed.getId());
        feedDTO.setText(feed.getText());
        feedDTO.setImagesUris(feed.getImageUrisList());
        feedDTO.setFromHost(Objects.nonNull(userMessage) && userMessage.getId().equals(islandMessage.getHostId()));
        feedDTO.setLikesCount(feed.getLikesCount());
        feedDTO.setCommentsCount(feed.getCommentsCount());
        feedDTO.setComments(feed.getLastCommentsList()
                .stream()
                .map(this.commentDTOFactory::valueOf)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        feedDTO.setRepostCount(feed.getRepostCount());
        feedDTO.setCreatedAt(feed.getCreatedAt());
        feedDTO.setIsLiked(feed.getIsLiked());

        feedDTO.setUser(this.userDTOFactory.briefValueOf(userMessage));
        feedDTO.setIsland(this.islandDTOFactory.briefValueOf(islandMessage));

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

        IslandMessage islandMessage = this.islandService.retrieveIslandById(feed.getIslandId());
        UserMessage userMessage = this.userService.retrieveUserById(feed.getUserId());

        BriefFeedDTO briefFeedDTO = new BriefFeedDTO();
        briefFeedDTO.setId(feed.getId());
        briefFeedDTO.setText(feed.getText());
        briefFeedDTO.setImagesUris(feed.getImageUrisList());
        briefFeedDTO.setFromHost(Objects.nonNull(userMessage) && userMessage.getId().equals(islandMessage.getHostId()));
        briefFeedDTO.setCreatedAt(feed.getCreatedAt());

        briefFeedDTO.setUser(this.userDTOFactory.briefValueOf(userMessage));
        briefFeedDTO.setIsland(this.islandDTOFactory.briefValueOf(islandMessage));

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
