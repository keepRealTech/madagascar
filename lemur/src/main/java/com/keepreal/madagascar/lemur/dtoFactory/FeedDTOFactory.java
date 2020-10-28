package com.keepreal.madagascar.lemur.dtoFactory;

import com.keepreal.madagascar.common.FeedGroupMessage;
import com.keepreal.madagascar.common.FeedMessage;
import com.keepreal.madagascar.common.IslandMessage;
import com.keepreal.madagascar.common.MediaType;
import com.keepreal.madagascar.common.Picture;
import com.keepreal.madagascar.common.UserMessage;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.coua.CheckNewFeedsMessage;
import com.keepreal.madagascar.coua.MembershipMessage;
import com.keepreal.madagascar.lemur.converter.MediaTypeConverter;
import com.keepreal.madagascar.lemur.service.EhcacheService;
import com.keepreal.madagascar.lemur.service.IslandService;
import com.keepreal.madagascar.lemur.service.MembershipService;
import com.keepreal.madagascar.lemur.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import swagger.model.BriefFeedDTO;
import swagger.model.CheckFeedsDTO;
import swagger.model.FeedDTO;
import swagger.model.FeedGroupInfo;
import swagger.model.FullFeedDTO;
import swagger.model.PosterFeedDTO;
import swagger.model.SnapshotFeedDTO;

import java.util.Collections;
import java.util.List;
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
    private final MembershipService membershipService;
    private final MembershipDTOFactory membershipDTOFactory;
    private final MultiMediaDTOFactory multiMediaDTOFactory;

    /**
     * Constructs the feed dto factory.
     *
     * @param islandService        {@link IslandService}.
     * @param islandDTOFactory     {@link IslandDTOFactory}.
     * @param userService          {@link UserService}.
     * @param userDTOFactory       {@link UserDTOFactory}.
     * @param commentDTOFactory    {@link CommentDTOFactory}.
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
        this.membershipService = membershipService;
        this.membershipDTOFactory = membershipDTOFactory;
        this.multiMediaDTOFactory = new MultiMediaDTOFactory(userService, userDTOFactory);
    }

    /**
     * Converts the {@link FeedMessage} into {@link FeedDTO}.
     *
     * @param feed        {@link FeedMessage}.
     * @param memberships {@link MembershipMessage}.
     * @return {@link FeedDTO}.
     */
    public FeedDTO valueOf(FeedMessage feed,
                           List<MembershipMessage> memberships) {
        return this.valueOf(feed, memberships, true);
    }

    /**
     * Converts the {@link FeedMessage} into {@link FeedDTO}.
     *
     * @param feed              {@link FeedMessage}.
     * @param memberships       {@link MembershipMessage}.
     * @param includeChargeable Whether includes the chargeable feeds.
     * @return {@link FeedDTO}.
     */
    public FeedDTO valueOf(FeedMessage feed,
                           List<MembershipMessage> memberships,
                           Boolean includeChargeable) {
        if (Objects.isNull(feed)
                || (!includeChargeable && feed.getPriceInCents() > 0L)) {
            return null;
        }
        return this.valueOf(feed, memberships, includeChargeable, feed.getCreatedAt());
    }

    /**
     * Converts the {@link FeedMessage} into {@link FeedDTO}.
     *
     * @param feed              {@link FeedMessage}.
     * @param memberships       {@link MembershipMessage}.
     * @param includeChargeable Whether includes the chargeable feeds.
     * @param recommendatedAt   Recommendated at.
     * @return {@link FeedDTO}.
     */
    public FeedDTO valueOf(FeedMessage feed,
                           List<MembershipMessage> memberships,
                           Boolean includeChargeable,
                           Long recommendatedAt) {
        if (Objects.isNull(feed)
                || (!includeChargeable && feed.getPriceInCents() > 0L)) {
            return null;
        }
        return this.valueOf(feed, memberships, includeChargeable, recommendatedAt, null);
    }

    /**
     * Converts the {@link FeedMessage} into {@link FeedDTO}.
     *
     * @param feed              {@link FeedMessage}.
     * @param memberships       {@link MembershipMessage}.
     * @param includeChargeable Whether includes the chargeable feeds.
     * @param recommendatedAt   Recommendated at.
     * @param feedgroup         {@link FeedGroupMessage}.
     * @return {@link FeedDTO}.
     */
    public FeedDTO valueOf(FeedMessage feed,
                           List<MembershipMessage> memberships,
                           Boolean includeChargeable,
                           Long recommendatedAt,
                           FeedGroupMessage feedgroup) {
        if (Objects.isNull(feed)
                || (!includeChargeable && feed.getPriceInCents() > 0L)) {
            return null;
        }

        try {
            IslandMessage islandMessage = this.islandService.retrieveIslandById(feed.getIslandId());
            UserMessage userMessage = this.userService.retrieveUserById(feed.getUserId());

            FeedDTO feedDTO = new FeedDTO();
            feedDTO.setId(feed.getId());
            feedDTO.setTitle(feed.getTitle());
            feedDTO.setBrief(feed.getBrief());
            feedDTO.setText(feed.getText());
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
            feedDTO.setIsMembership(feed.getIsMembership());
            feedDTO.setIsChargeable(feed.getPriceInCents() > 0L);
            feedDTO.setIsTop(feed.getIsTop());
            feedDTO.setMediaType(MediaTypeConverter.converToMultiMediaType(feed.getType()));
            feedDTO.setMultimedia(this.multiMediaDTOFactory.listValueOf(feed));
            feedDTO.setPriceInCents(feed.getPriceInCents());
            feedDTO.setCanSave(feed.getCanSave());
            feedDTO.setRecommendatedAt(recommendatedAt);
            boolean isPicType = feed.getType().equals(MediaType.MEDIA_PICS) || feed.getType().equals(MediaType.MEDIA_ALBUM);
            if (!CollectionUtils.isEmpty(feed.getImageUrisList())) {
                feedDTO.setImagesUris(feed.getImageUrisList());
            }

            if (CollectionUtils.isEmpty(feed.getImageUrisList()) && isPicType) {
                feedDTO.setImagesUris(feed.getPics().getPictureList().stream().map(Picture::getImgUrl).collect(Collectors.toList()));
            }

            if (Objects.nonNull(feedgroup)) {
                FeedGroupInfo feedGroupInfo = new FeedGroupInfo();
                feedGroupInfo.setId(feedgroup.getId());
                feedGroupInfo.setName(feedgroup.getName());
                feedDTO.setFeedGroupInfo(feedGroupInfo);
            }

            feedDTO.setIsAccess(feed.getIsAccess());
            if (!memberships.isEmpty()) {
                feedDTO.setIsMembership(true);
                feedDTO.setMembership(this.membershipDTOFactory.simpleValueOf(memberships.get(0)));
                feedDTO.setMembershipList(memberships.stream().map(this.membershipDTOFactory::simpleValueOf).collect(Collectors.toList()));
            } else if (!feed.getMembershipIdList().isEmpty()) {
                feedDTO.setIsAccess(true);
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
            briefFeedDTO.setTitle(feed.getTitle());
            briefFeedDTO.setBrief(feed.getBrief());
            briefFeedDTO.setText(feed.getText());
            briefFeedDTO.setFromHost(Objects.nonNull(userMessage) && userMessage.getId().equals(islandMessage.getHostId()));
            briefFeedDTO.setCreatedAt(feed.getCreatedAt());
            briefFeedDTO.setMediaType(MediaTypeConverter.converToMultiMediaType(feed.getType()));
            briefFeedDTO.setMultimedia(this.multiMediaDTOFactory.listValueOf(feed));
            briefFeedDTO.setPriceInCents(feed.getPriceInCents());

            boolean isPicType = feed.getType().equals(MediaType.MEDIA_PICS) || feed.getType().equals(MediaType.MEDIA_ALBUM);
            if (!CollectionUtils.isEmpty(feed.getImageUrisList())) {
                briefFeedDTO.setImagesUris(feed.getImageUrisList());
            }

            if (CollectionUtils.isEmpty(feed.getImageUrisList()) && isPicType) {
                briefFeedDTO.setImagesUris(feed.getPics().getPictureList().stream().map(Picture::getImgUrl).collect(Collectors.toList()));
            }

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
     * @param feed       {@link FeedMessage}.
     * @param subscribed Whether island subscribed.
     * @param hasAccess  Whether has access.
     * @param isDeleted  Whether feed deleted.
     * @return {@link SnapshotFeedDTO}.
     */
    public SnapshotFeedDTO snapshotValueOf(FeedMessage feed, boolean subscribed, boolean hasAccess, boolean isDeleted) {
        if (Objects.isNull(feed)) {
            return null;
        }

        IslandMessage islandMessage = this.islandService.retrieveIslandById(feed.getIslandId());
        UserMessage userMessage = this.userService.retrieveUserById(feed.getUserId());

        SnapshotFeedDTO snapshotFeedDTO = new SnapshotFeedDTO();
        snapshotFeedDTO.setId(feed.getId());
        snapshotFeedDTO.setTitle(feed.getTitle());
        snapshotFeedDTO.setBrief(feed.getBrief());
        snapshotFeedDTO.setText(feed.getText());
        snapshotFeedDTO.setImagesUris(feed.getImageUrisList());
        snapshotFeedDTO.setFromHost(Objects.nonNull(userMessage) && userMessage.getId().equals(islandMessage.getHostId()));
        snapshotFeedDTO.setCreatedAt(feed.getCreatedAt());
        snapshotFeedDTO.setIsDeleted(isDeleted);
        snapshotFeedDTO.setPriceInCents(feed.getPriceInCents());

        snapshotFeedDTO.setIsAccess(hasAccess);

        snapshotFeedDTO.setMediaType(MediaTypeConverter.converToMultiMediaType(feed.getType()));
        snapshotFeedDTO.setMultimedia(this.multiMediaDTOFactory.listValueOf(feed));

        boolean isPicType = feed.getType().equals(MediaType.MEDIA_PICS) || feed.getType().equals(MediaType.MEDIA_ALBUM);
        if (!CollectionUtils.isEmpty(feed.getImageUrisList())) {
            snapshotFeedDTO.setImagesUris(feed.getImageUrisList());
        }

        if (CollectionUtils.isEmpty(feed.getImageUrisList()) && isPicType) {
            snapshotFeedDTO.setImagesUris(feed.getPics().getPictureList().stream().map(Picture::getImgUrl).collect(Collectors.toList()));
        }

        snapshotFeedDTO.setUser(this.userDTOFactory.briefValueOf(userMessage));
        snapshotFeedDTO.setIsland(this.islandDTOFactory.briefValueOf(islandMessage));
        snapshotFeedDTO.setIsSubscribed(subscribed);

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

    /**
     * Builds a feed message with feed group infos.
     *
     * @param feed              {@link FeedMessage}.
     * @param feedGroup         {@link FeedGroupMessage}.
     * @param lastFeedId        Last feed id.
     * @param nextFeedId        Next feed id.
     * @param includeChargeable Whether includes the chargeable.
     * @return {@link FullFeedDTO}.
     */
    public FullFeedDTO valueOf(FeedMessage feed,
                               FeedGroupMessage feedGroup,
                               String lastFeedId,
                               String nextFeedId,
                               Boolean includeChargeable) {
        if (Objects.isNull(feed)) {
            return null;
        }

        try {
            IslandMessage islandMessage = this.islandService.retrieveIslandById(feed.getIslandId());
            UserMessage userMessage = this.userService.retrieveUserById(feed.getUserId());
            UserMessage hostMessage = this.userService.retrieveUserById(feed.getHostId());

            FullFeedDTO fullFeedDTO = new FullFeedDTO();
            fullFeedDTO.setId(feed.getId());
            fullFeedDTO.setTitle(feed.getTitle());
            fullFeedDTO.setBrief(feed.getBrief());
            fullFeedDTO.setText(feed.getText());
            fullFeedDTO.setFromHost(feed.getFromHost());
            fullFeedDTO.setLikesCount(feed.getLikesCount());
            fullFeedDTO.setCommentsCount(feed.getCommentsCount());
            fullFeedDTO.setComments(feed.getLastCommentsList()
                    .stream()
                    .map(this.commentDTOFactory::valueOf)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList()));
            fullFeedDTO.setRepostCount(feed.getRepostCount());
            fullFeedDTO.setCreatedAt(feed.getCreatedAt());
            fullFeedDTO.setIsLiked(feed.getIsLiked());
            fullFeedDTO.setIsMembership(feed.getIsMembership());
            fullFeedDTO.setIsChargeable(feed.getPriceInCents() > 0L);
            fullFeedDTO.setIsTop(feed.getIsTop());
            fullFeedDTO.setMediaType(MediaTypeConverter.converToMultiMediaType(feed.getType()));
            fullFeedDTO.setMultimedia(this.multiMediaDTOFactory.listValueOf(feed));
            fullFeedDTO.setPriceInCents(feed.getPriceInCents());
            fullFeedDTO.setCanSave(feed.getCanSave());
            if (!StringUtils.isEmpty(feedGroup.getId())) {
                FeedGroupInfo feedGroupInfo = new FeedGroupInfo();
                feedGroupInfo.setId(feedGroup.getId());
                feedGroupInfo.setName(feedGroup.getName());
                feedGroupInfo.setLastFeedId(lastFeedId);
                feedGroupInfo.setNextFeedId(nextFeedId);
                fullFeedDTO.setFeedGroupInfo(feedGroupInfo);
            }

            boolean isPicType = feed.getType().equals(MediaType.MEDIA_PICS) || feed.getType().equals(MediaType.MEDIA_ALBUM);
            if (!CollectionUtils.isEmpty(feed.getImageUrisList())) {
                fullFeedDTO.setImagesUris(feed.getImageUrisList());
            }

            if (CollectionUtils.isEmpty(feed.getImageUrisList()) && isPicType) {
                fullFeedDTO.setImagesUris(feed.getPics().getPictureList().stream().map(Picture::getImgUrl).collect(Collectors.toList()));
            }

            fullFeedDTO.setIsAccess(true);
            if (!CollectionUtils.isEmpty(feed.getMembershipIdList())) {
                List<MembershipMessage> membershipMessages = this.membershipService.retrieveMembershipsByIds(feed.getMembershipIdList());
                fullFeedDTO.setIsMembership(!CollectionUtils.isEmpty(membershipMessages));
                fullFeedDTO.setIsAccess(feed.getIsAccess() || membershipMessages.isEmpty());

                if (!membershipMessages.isEmpty()) {
                    fullFeedDTO.setMembership(this.membershipDTOFactory.simpleValueOf(membershipMessages.get(0)));
                    fullFeedDTO.setMembershipList(membershipMessages.stream().map(this.membershipDTOFactory::simpleValueOf).collect(Collectors.toList()));
                }
            } else {
                fullFeedDTO.setIsAccess(feed.getIsAccess());
            }

            fullFeedDTO.setUser(this.userDTOFactory.briefValueOf(userMessage));
            fullFeedDTO.setHost(this.userDTOFactory.briefValueOf(hostMessage));
            fullFeedDTO.setIsland(this.islandDTOFactory.briefValueOf(islandMessage));

            if (feed.getPriceInCents() > 0 && !includeChargeable) {
                fullFeedDTO.setMultimedia(Collections.emptyList());
                fullFeedDTO.setText("该版本不支持此动态，请升级试试喔");
            }

            return fullFeedDTO;
        } catch (KeepRealBusinessException exception) {
            log.error("Failed to serialize feed {}, cause {}.", feed.getId(), exception.getErrorCode());
            return null;
        }
    }

}
