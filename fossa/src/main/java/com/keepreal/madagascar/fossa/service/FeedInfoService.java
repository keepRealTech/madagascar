package com.keepreal.madagascar.fossa.service;

import com.keepreal.madagascar.common.CommentMessage;
import com.keepreal.madagascar.common.FeedMessage;
import com.keepreal.madagascar.common.MediaType;
import com.keepreal.madagascar.common.Picture;
import com.keepreal.madagascar.common.PicturesMessage;
import com.keepreal.madagascar.common.ReactionType;
import com.keepreal.madagascar.common.constants.Constants;
import com.keepreal.madagascar.fossa.TimelineFeedMessage;
import com.keepreal.madagascar.fossa.dao.FeedInfoRepository;
import com.keepreal.madagascar.fossa.dao.ReactionRepository;
import com.keepreal.madagascar.fossa.model.AnswerInfo;
import com.keepreal.madagascar.fossa.model.FeedInfo;
import com.keepreal.madagascar.fossa.util.MediaMessageConvertUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.Comparator;
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

    private final MongoTemplate mongoTemplate;
    private final CommentService commentService;
    private final FeedInfoRepository feedInfoRepository;
    private final ReactionRepository reactionRepository;
    private final SubscribeMembershipService subscribeMembershipService;
    private final FeedChargeService feedChargeService;

    /**
     * Constructs the feed service
     *
     * @param mongoTemplate              {@link MongoTemplate}.
     * @param commentService             {@link CommentService}.
     * @param feedInfoRepository         {@link FeedInfoRepository}.
     * @param reactionRepository         {@link ReactionRepository}.
     * @param subscribeMembershipService {@link SubscribeMembershipService}.
     * @param feedChargeService          {@link FeedChargeService}.
     */
    public FeedInfoService(MongoTemplate mongoTemplate,
                           CommentService commentService,
                           FeedInfoRepository feedInfoRepository,
                           ReactionRepository reactionRepository,
                           SubscribeMembershipService subscribeMembershipService,
                           FeedChargeService feedChargeService) {
        this.mongoTemplate = mongoTemplate;
        this.commentService = commentService;
        this.feedInfoRepository = feedInfoRepository;
        this.reactionRepository = reactionRepository;
        this.subscribeMembershipService = subscribeMembershipService;
        this.feedChargeService = feedChargeService;
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
     * @param userId   User id (decide is liked).
     * @return {@link FeedMessage}.
     */
    public FeedMessage getFeedMessage(FeedInfo feedInfo,
                                      String userId) {
        if (Objects.isNull(feedInfo)) {
            return null;
        }

        List<String> myMembershipIds = subscribeMembershipService.retrieveMembershipIds(userId, feedInfo.getIslandId());
        return this.getFeedMessage(feedInfo, userId, myMembershipIds);
    }

    /**
     * Retrieves the feed message.
     *
     * @param feedInfo        {@link FeedInfo}.
     * @param userId          User id (decide is liked).
     * @param myMembershipIds User valid membership ids.
     * @return {@link FeedMessage}.
     */
    public FeedMessage getFeedMessage(FeedInfo feedInfo,
                                      String userId,
                                      List<String> myMembershipIds) {
        if (Objects.isNull(feedInfo)) {
            return null;
        }

        boolean isLiked = reactionRepository.existsByFeedIdAndUserIdAndReactionTypeListContains(feedInfo.getId(), userId, ReactionType.REACTION_LIKE_VALUE);

        List<CommentMessage> lastCommentMessage = this.commentService.getCommentsMessage(feedInfo.getId(), Constants.DEFAULT_FEED_LAST_COMMENT_COUNT);

        return this.getFeedMessage(feedInfo, userId, myMembershipIds, lastCommentMessage, isLiked);
    }

    /**
     * Retrieves the feed message.
     *
     * @param feedInfo            {@link FeedInfo}.
     * @param userId              User id (decide is liked).
     * @param myMembershipIds     User valid membership ids.
     * @param lastCommentMessages Last comment messages.
     * @param isLiked             Feed liked by user or not.
     * @return {@link FeedMessage}.
     */
    public FeedMessage getFeedMessage(FeedInfo feedInfo,
                                      String userId,
                                      List<String> myMembershipIds,
                                      List<CommentMessage> lastCommentMessages,
                                      boolean isLiked) {
        if (Objects.isNull(feedInfo)) {
            return null;
        }

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
                .addAllLastComments(lastCommentMessages)
                .setIsLiked(isLiked)
                .setIsDeleted(feedInfo.getDeleted())
                .setPriceInCents(Objects.nonNull(feedInfo.getPriceInCents()) ? feedInfo.getPriceInCents() : 0L)
                .setFromHost(feedInfo.getFromHost() == null ? false : feedInfo.getFromHost())
                .setIsTop(feedInfo.getIsTop() == null ? false : feedInfo.getIsTop())
                .setHostId(feedInfo.getHostId())
                .setCanSave(feedInfo.getCanSave() == null ? false : feedInfo.getCanSave())
                .setFeedgroupId(feedInfo.getFeedGroupId());

        List<String> membershipIds = feedInfo.getMembershipIds();
        if (Objects.isNull(membershipIds) || membershipIds.size() == 0) {
            if (Objects.nonNull(feedInfo.getPriceInCents()) && feedInfo.getPriceInCents() > 0L) {
                builder.setIsAccess(feedInfo.getHostId().equals(userId) || this.feedChargeService.retrieveFeedChargeAccess(userId, feedInfo.getId()));
            } else {
                builder.setIsAccess(true);
            }
            builder.addAllMembershipId(Collections.emptyList());
            builder.setIsMembership(false);
        } else {
            builder.setIsMembership(true);
            builder.setIsAccess(false);
            if (userId.equals(feedInfo.getHostId()) || membershipIds.stream().anyMatch(myMembershipIds::contains)) {
                builder.setIsAccess(true);
            }
            membershipIds.sort((o1, o2) -> {
                boolean co1 = myMembershipIds.contains(o1);
                boolean co2 = myMembershipIds.contains(o2);
                if (co1 && !co2) {
                    return -1;
                } else if (co2 && !co1){
                    return 1;
                } else {
                    return 0;
                }
            });
            builder.addAllMembershipId(membershipIds);
        }
        this.processMedia(builder, feedInfo);

        builder.addAllUserMembershipId(CollectionUtils.isEmpty(feedInfo.getUserMembershipIds()) ? Collections.emptyList() : feedInfo.getUserMembershipIds());

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
     * @param feedInfo {@link FeedInfo}.
     * @return {@link FeedInfo}.
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
    public FeedInfo insert(FeedInfo feedInfo) {
        return feedInfoRepository.insert(feedInfo);
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
     * Retrieves feeds by feed group id.
     *
     * @param feedGroupId Feed group id.
     * @param pageable    {@link Pageable}.
     * @return {@link FeedInfo}.
     */
    public Page<FeedInfo> retrieveFeedsByFeedGroupId(String feedGroupId, Pageable pageable) {
        return this.feedInfoRepository.findAllByFeedGroupIdAndDeletedIsFalseOrderByCreatedTimeDesc(feedGroupId, pageable);
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
     * 将H5手机用户的提问箱信息合并到微信用户
     *
     * @param wechatUserId    wechat user id
     * @param webMobileUserId web mobile user id
     */
    @Transactional
    public void mergeUserBoxInfo(String wechatUserId, String webMobileUserId) throws RuntimeException {
        int page = 0;
        int pageSize = 100;

        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(webMobileUserId));
        query.addCriteria(Criteria.where("multiMediaType").is(MediaType.MEDIA_QUESTION.name()));

        long totalCount = mongoTemplate.count(query, FeedInfo.class);
        do {
            List<FeedInfo> feedInfoList = mongoTemplate.find(query.with(PageRequest.of(page, pageSize)), FeedInfo.class);
            feedInfoList.forEach(feedInfo -> feedInfo.setUserId(wechatUserId));
            this.feedInfoRepository.saveAll(feedInfoList);
            ++page;
        } while (totalCount / pageSize >= page);
    }

    /**
     * Processes the multimedia.
     *
     * @param builder  {@link FeedMessage.Builder}.
     * @param feedInfo {@link FeedInfo}.
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
                if (!feedInfo.getMediaInfos().isEmpty()) {
                    builder.setVideo(MediaMessageConvertUtils.toVideoMessage(feedInfo.getMediaInfos().get(0)));
                }
                break;
            case MEDIA_AUDIO:
                if (!feedInfo.getMediaInfos().isEmpty()) {
                    builder.setAudio(MediaMessageConvertUtils.toAudioMessage(feedInfo.getMediaInfos().get(0)));
                }
                break;
            case MEDIA_HTML:
                if (!feedInfo.getMediaInfos().isEmpty()) {
                    builder.setHtml(MediaMessageConvertUtils.toHtmlMessage(feedInfo.getMediaInfos().get(0)));
                }
                break;
            case MEDIA_QUESTION:
                if (!feedInfo.getMediaInfos().isEmpty()) {
                    builder.setAnswer(MediaMessageConvertUtils.toAnswerMessage((AnswerInfo) feedInfo.getMediaInfos().get(0)));
                }
                break;
        }
    }
}
