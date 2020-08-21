package com.keepreal.madagascar.fossa.service;

import com.keepreal.madagascar.common.CommentMessage;
import com.keepreal.madagascar.common.FeedMessage;
import com.keepreal.madagascar.common.MediaType;
import com.keepreal.madagascar.common.Picture;
import com.keepreal.madagascar.common.PicturesMessage;
import com.keepreal.madagascar.common.ReactionType;
import com.keepreal.madagascar.fossa.TimelineFeedMessage;
import com.keepreal.madagascar.fossa.dao.FeedInfoRepository;
import com.keepreal.madagascar.fossa.dao.ReactionRepository;
import com.keepreal.madagascar.fossa.model.AnswerInfo;
import com.keepreal.madagascar.fossa.model.FeedInfo;
import com.keepreal.madagascar.fossa.util.MediaMessageConvertUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Represents the feed service
 */
@Slf4j
@Service
public class FeedInfoService {

    private static final int DEFAULT_LAST_COMMENT_COUNT = 2;

    private final MongoTemplate mongoTemplate;
    private final CommentService commentService;
    private final FeedInfoRepository feedInfoRepository;
    private final ReactionRepository reactionRepository;
    private final SubscribeMembershipService subscribeMembershipService;

    /**
     * Constructs the feed service
     *
     * @param mongoTemplate              {@link MongoTemplate}.
     * @param commentService             {@link CommentService}.
     * @param feedInfoRepository         {@link FeedInfoRepository}.
     * @param reactionRepository         {@link ReactionRepository}.
     * @param subscribeMembershipService {@link SubscribeMembershipService}.
     */
    public FeedInfoService(MongoTemplate mongoTemplate,
                           CommentService commentService,
                           FeedInfoRepository feedInfoRepository,
                           ReactionRepository reactionRepository,
                           SubscribeMembershipService subscribeMembershipService) {
        this.mongoTemplate = mongoTemplate;
        this.commentService = commentService;
        this.feedInfoRepository = feedInfoRepository;
        this.reactionRepository = reactionRepository;
        this.subscribeMembershipService = subscribeMembershipService;
    }

    /**
     * Delete feed by id.
     *
     * @param id feed id.
     */
    public void deleteFeedById(String id) {
        mongoTemplate.updateFirst(
                Query.query(Criteria.where("id").is(id)),
                Update.update("deleted", true),
                FeedInfo.class);
    }

    /**
     * Retrieves feed message by id.
     *
     * @param feedId feed id.
     * @param userId user id.
     * @return {@link FeedMessage}.
     */
    public FeedMessage getFeedMessageById(String feedId, String userId) {
        Optional<FeedInfo> feedInfoOptional = this.feedInfoRepository.findById(feedId);
        if (feedInfoOptional.isPresent()) {
            return this.getFeedMessage(feedInfoOptional.get(), userId);
        } else {
            return FeedMessage.newBuilder().build();
        }
    }

    /**
     * Feed count add one by feed id and type
     *
     * @param feedId feed id.
     * @param type   type to be changed.
     */
    public void incFeedCount(String feedId, String type) {
        this.updateFeedCountByType(feedId, type, 1);
    }

    /**
     * Feed count sub one by feed id and type
     * todo negative count
     *
     * @param feedId feed id.
     * @param type   type to be changed.
     */
    public void subFeedCount(String feedId, String type) {
        this.updateFeedCountByType(feedId, type, -1);
    }

    /**
     * Change feed count by feed id and type
     *
     * @param feedId feed id.
     * @param type   type to be changed.
     * @param count  add or sub count.
     */
    private void updateFeedCountByType(String feedId, String type, Integer count) {
        Update update = new Update();
        update.inc(type, count);
        mongoTemplate.updateFirst(Query.query(Criteria.where("id").is(feedId)), update, FeedInfo.class);
    }

    public TimelineFeedMessage getTimelineFeedMessage(FeedInfo feedInfo) {
        if (feedInfo == null)
            return null;
        String duplicateTag = StringUtils.isEmpty(feedInfo.getDuplicateTag()) ?
                UUID.randomUUID().toString() :
                feedInfo.getDuplicateTag();
        return TimelineFeedMessage.newBuilder().setId(feedInfo.getId())
                .setIslandId(feedInfo.getIslandId())
                .setCreatedAt(feedInfo.getCreatedTime())
                .setDuplicateTag(duplicateTag).build();
    }

    /**
     * Retrieves the feed message.
     *
     * @param feedInfo {@link FeedInfo}.
     * @param userId   user id (decide is liked).
     * @return {@link FeedMessage}.
     */
    public FeedMessage getFeedMessage(FeedInfo feedInfo, String userId) {
        if (feedInfo == null)
            return null;

        List<CommentMessage> lastCommentMessage = commentService.getCommentsMessage(feedInfo.getId(), DEFAULT_LAST_COMMENT_COUNT);
        boolean isLiked = reactionRepository.existsByFeedIdAndUserIdAndReactionTypeListContains(feedInfo.getId(), userId, ReactionType.REACTION_LIKE_VALUE);
        FeedMessage.Builder builder = FeedMessage.newBuilder()
                .setId(feedInfo.getId())
                .setIslandId(feedInfo.getIslandId())
                .setUserId(feedInfo.getUserId())
                .setText(feedInfo.getText())
                .addAllImageUris(feedInfo.getImageUrls() == null ? Collections.emptyList() : feedInfo.getImageUrls())
                .setCreatedAt(feedInfo.getCreatedTime())
                .setCommentsCount(feedInfo.getCommentsCount())
                .setLikesCount(feedInfo.getLikesCount() < 0 ? 0 : feedInfo.getLikesCount())
                .setRepostCount(feedInfo.getRepostCount())
                .addAllLastComments(lastCommentMessage)
                .setIsLiked(isLiked)
                .setIsDeleted(feedInfo.getDeleted())
                .setPriceInCents(Objects.nonNull(feedInfo.getPriceInCents()) ? feedInfo.getPriceInCents() : 0L)
                .setFromHost(feedInfo.getFromHost() == null ? false : feedInfo.getFromHost())
                .setIsTop(feedInfo.getIsTop() == null ? false : feedInfo.getIsTop());

        List<String> membershipIds = feedInfo.getMembershipIds();
        if (Objects.isNull(membershipIds) || membershipIds.size() == 0) {
            builder.setIsAccess(true);
            builder.addAllMembershipId(Collections.emptyList());
            builder.setIsMembership(false);
        } else {
            builder.setIsMembership(true);
            List<String> myMembershipIds = subscribeMembershipService.retrieveMembershipIds(userId, feedInfo.getIslandId());
            if (userId.equals(feedInfo.getHostId()) || membershipIds.stream().anyMatch(myMembershipIds::contains)) {
                builder.setIsAccess(true);
                builder.addAllMembershipId(membershipIds);
            } else {
                builder.setIsAccess(false);
                builder.addAllMembershipId(membershipIds);
            }
        }
        this.processMedia(builder, feedInfo);

        return builder.build();
    }

    /**
     * Inserts the feeds.
     *
     * @param feedInfoList {@link FeedInfo}.
     */
    public List<FeedInfo> saveAll(List<FeedInfo> feedInfoList) {
        return feedInfoRepository.saveAll(feedInfoList);
    }

    /**
     * Update feed.
     *
     * @param feedInfo  {@link FeedInfo}.
     * @return  {@link FeedInfo}.
     */
    public FeedInfo update(FeedInfo feedInfo) {
        return feedInfoRepository.save(feedInfo);
    }

    /**
     * Retrieves feed by id.
     *
     * @param feedId         feed id.
     * @param includeDeleted Whether includes the deleted.
     * @return {@link FeedInfo}.
     */
    public FeedInfo findFeedInfoById(String feedId, boolean includeDeleted) {
        if (includeDeleted) {
            return feedInfoRepository.findFeedInfoById(feedId);
        }

        return feedInfoRepository.findFeedInfoByIdAndDeletedIsFalse(feedId);
    }

    /**
     * Retrieves latest feed by user id.
     *
     * @param userId user id.
     * @return {@link FeedInfo}
     */
    public FeedInfo findTopByUserIdAndDeletedIsFalseOrderByCreatedTimeDesc(String userId) {
        return feedInfoRepository.findTopByUserIdAndDeletedIsFalseOrderByCreatedTimeDesc(userId);
    }

    /**
     * Inserts the feed.
     *
     * @param feedInfo {@link FeedInfo}
     */
    public void insert(FeedInfo feedInfo) {
        feedInfoRepository.insert(feedInfo);
    }

    /**
     * Retrieves a list of feeds by ids.
     *
     * @param ids Feed ids.
     * @return List of {@link FeedInfo}.
     */
    public List<FeedInfo> findByIds(Iterable<String> ids) {
        return this.feedInfoRepository.findAllByIdInAndDeletedIsFalseOrderByCreatedTimeDesc(ids);
    }

    /**
     * top feed by feed id
     *
     * @param feedId feed id
     */
    public void topFeedById(String feedId) {
        mongoTemplate.updateFirst(
                Query.query(Criteria.where("id").is(feedId)),
                Update.update("isTop", true).set("toppedTime", System.currentTimeMillis()),
                FeedInfo.class);
    }

    /**
     * cancel topped feed by feed id
     *
     * @param feedId feed id
     */
    public void cancelToppedFeedById(String feedId) {
        mongoTemplate.updateFirst(
                Query.query(Criteria.where("id").is(feedId)),
                Update.update("isTop", false),
                FeedInfo.class);
    }

    /**
     * find this island topped feed and cancel it
     *
     * @param islandId island id
     */
    public void cancelToppedFeedByIslandId(String islandId) {
        FeedInfo feedInfo = findToppedFeedByIslandId(islandId);
        if (Objects.nonNull(feedInfo)) {
            cancelToppedFeedById(feedInfo.getId());
        }
    }

    /**
     * find topped feed (only one in this version (v1.2))
     *
     * @return feed information
     */
    public FeedInfo findToppedFeedByIslandId(String islandId) {
        return this.feedInfoRepository.findTopByIslandIdAndIsTopIsTrueAndDeletedIsFalse(islandId);
    }

    /**
     * Processes the multimedia.
     *
     * @param builder   {@link FeedMessage.Builder}.
     * @param feedInfo  {@link FeedInfo}.
     */
    private void processMedia(FeedMessage.Builder builder, FeedInfo feedInfo) {
        if (feedInfo.getMultiMediaType() == null) {
            if (CollectionUtils.isEmpty(feedInfo.getImageUrls())) {
                builder.setType(MediaType.MEDIA_TEXT);
            } else {
                List<String> imageUrls = feedInfo.getImageUrls();
                builder.setType(MediaType.MEDIA_PICS);
                builder.setPics(PicturesMessage.newBuilder()
                        .addAllPicture(
                                imageUrls.stream()
                                        .map(url -> Picture.newBuilder()
                                                .setImgUrl(url)
                                                .setHeight(0)
                                                .setWidth(0)
                                                .setSize(0)
                                                .build())
                                        .collect(Collectors.toList())).build());
            }
            return;
        }
        MediaType mediaType = MediaType.valueOf(feedInfo.getMultiMediaType());
        builder.setType(mediaType);
        switch (mediaType) {
            case MEDIA_PICS:
            case MEDIA_ALBUM:
                builder.setPics(MediaMessageConvertUtils.toPicturesMessage(feedInfo.getMediaInfos()));
                break;
            case MEDIA_VIDEO:
                builder.setVideo(MediaMessageConvertUtils.toVideoMessage(feedInfo.getMediaInfos().get(0)));
                break;
            case MEDIA_AUDIO:
                builder.setAudio(MediaMessageConvertUtils.toAudioMessage(feedInfo.getMediaInfos().get(0)));
                break;
            case MEDIA_HTML:
                builder.setHtml(MediaMessageConvertUtils.toHtmlMessage(feedInfo.getMediaInfos().get(0)));
                break;
            case MEDIA_QUESTION:
                builder.setAnswer(MediaMessageConvertUtils.toAnswerMessage((AnswerInfo) feedInfo.getMediaInfos().get(0)));
        }
    }
}
