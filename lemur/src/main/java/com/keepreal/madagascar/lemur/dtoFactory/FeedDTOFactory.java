package com.keepreal.madagascar.lemur.dtoFactory;

import com.keepreal.madagascar.common.FeedMessage;
import com.keepreal.madagascar.common.IslandMessage;
import com.keepreal.madagascar.common.UserMessage;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.coua.CheckNewFeedsMessage;
import com.keepreal.madagascar.coua.MembershipMessage;
import com.keepreal.madagascar.lemur.service.EhcacheService;
import com.keepreal.madagascar.lemur.service.IslandService;
import com.keepreal.madagascar.lemur.service.MembershipService;
import com.keepreal.madagascar.lemur.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import swagger.model.BriefFeedDTO;
import swagger.model.CheckFeedsDTO;
import swagger.model.FeedDTO;
import swagger.model.PosterFeedDTO;
import swagger.model.SnapshotFeedDTO;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents the feed dto factory.
 */
@Component
@Slf4j
public class FeedDTOFactory {

    private final IslandService islandService;
    private final IslandDTOFactory islandDTOFactory;
    private final UserService userService;
    private final UserDTOFactory userDTOFactory;
    private final CommentDTOFactory commentDTOFactory;
    private final EhcacheService ehcacheService;
    private final MembershipService membershipService;
    private final MembershipDTOFactory membershipDTOFactory;

    /**
     * Constructs the feed dto factory.
     *
     * @param islandService        {@link IslandService}.
     * @param islandDTOFactory     {@link IslandDTOFactory}.
     * @param userService          {@link UserService}.
     * @param userDTOFactory       {@link UserDTOFactory}.
     * @param commentDTOFactory    {@link CommentDTOFactory}.
     * @param ehcacheService       {@link EhcacheService}.
     * @param membershipService    {@link MembershipService}.
     * @param membershipDTOFactory {@link MembershipDTOFactory}.
     */
    public FeedDTOFactory(IslandService islandService,
                          IslandDTOFactory islandDTOFactory,
                          UserService userService,
                          UserDTOFactory userDTOFactory,
                          CommentDTOFactory commentDTOFactory,
                          EhcacheService ehcacheService,
                          MembershipService membershipService,
                          MembershipDTOFactory membershipDTOFactory) {
        this.islandService = islandService;
        this.islandDTOFactory = islandDTOFactory;
        this.userService = userService;
        this.userDTOFactory = userDTOFactory;
        this.commentDTOFactory = commentDTOFactory;
        this.ehcacheService = ehcacheService;
        this.membershipService = membershipService;
        this.membershipDTOFactory = membershipDTOFactory;
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

        try {
            IslandMessage islandMessage = this.islandService.retrieveIslandById(feed.getIslandId());
            UserMessage userMessage = this.userService.retrieveUserById(feed.getUserId());

            FeedDTO feedDTO = new FeedDTO();
            feedDTO.setId(feed.getId());
            feedDTO.setText(feed.getText());
            feedDTO.setImagesUris(feed.getImageUrisList());
            feedDTO.setFromHost(feed.getFromHost());
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
            feedDTO.setIsAccess(feed.getIsAccess());
            feedDTO.setIsMembership(feed.getIsMembership());
            if (feed.getIsMembership()) {
                MembershipMessage membershipMessage = this.membershipService.retrieveMembershipById(feed.getMembershipId());
                feedDTO.setMembership(this.membershipDTOFactory.simpleValueOf(membershipMessage));
            }
            feedDTO.setUser(this.userDTOFactory.briefValueOf(userMessage));
            feedDTO.setIsland(this.islandDTOFactory.briefValueOf(islandMessage));

            return feedDTO;
        } catch (KeepRealBusinessException exception) {
            log.error("Failed to serialize feed {}, cause {}.", feed.getId(), exception.getErrorCode());
            return null;
        }
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

        try {
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
        } catch (KeepRealBusinessException exception) {
            log.error("Failed to serialize feed {}.", feed.getId());
            return null;
        }
    }

    /**
     * Converts the {@link FeedMessage} into {@link SnapshotFeedDTO}.
     *
     * @param feed {@link FeedMessage}.
     * @return {@link SnapshotFeedDTO}.
     */
    public SnapshotFeedDTO snapshotValueOf(FeedMessage feed) {
        if (Objects.isNull(feed)) {
            return null;
        }

        IslandMessage islandMessage = this.islandService.retrieveIslandById(feed.getIslandId());
        UserMessage userMessage = this.userService.retrieveUserById(feed.getUserId());
        Map<String, Boolean> stateMap = this.islandService.retrieveIslandSubscribeStateByUserId(
                feed.getUserId(), Collections.singletonList(feed.getIslandId()));

        SnapshotFeedDTO snapshotFeedDTO = new SnapshotFeedDTO();
        snapshotFeedDTO.setId(feed.getId());
        snapshotFeedDTO.setText(feed.getText());
        snapshotFeedDTO.setImagesUris(feed.getImageUrisList());
        snapshotFeedDTO.setFromHost(Objects.nonNull(userMessage) && userMessage.getId().equals(islandMessage.getHostId()));
        snapshotFeedDTO.setCreatedAt(feed.getCreatedAt());
        snapshotFeedDTO.setIsDeleted(this.ehcacheService.checkFeedDeleted(feed.getId()));

        snapshotFeedDTO.setUser(this.userDTOFactory.briefValueOf(userMessage));
        snapshotFeedDTO.setIsland(this.islandDTOFactory.briefValueOf(islandMessage));
        Boolean isSubscribed = stateMap.get(feed.getIslandId());
        snapshotFeedDTO.setIsSubscribed(isSubscribed == null ? false : isSubscribed);

        return snapshotFeedDTO;
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
        checkFeedsDTO.setIslandId(checkNewFeeds.getIslandId());
        checkFeedsDTO.setHasNew(checkNewFeeds.getHasNewFeeds());

        return checkFeedsDTO;
    }

    /**
     * Converts {@link FeedMessage} into {@link PosterFeedDTO}.
     *
     * @param feed {@link FeedMessage}.
     * @return {@link PosterFeedDTO}.
     */
    public PosterFeedDTO posterValueOf(FeedMessage feed) {
        if (Objects.isNull(feed)) {
            return null;
        }
        PosterFeedDTO posterFeedDTO = new PosterFeedDTO();
        posterFeedDTO.setId(feed.getId());
        posterFeedDTO.setUser(this.userDTOFactory.briefValueOf(this.userService.retrieveUserById(feed.getUserId())));
        posterFeedDTO.setText(feed.getText());
        posterFeedDTO.setImagesUris(feed.getImageUrisList());
        posterFeedDTO.setCreatedAt(feed.getCreatedAt());
        return posterFeedDTO;
    }

}
