package com.keepreal.madagascar.fossa.service;

import com.keepreal.madagascar.common.CommentMessage;
import com.keepreal.madagascar.common.FeedMessage;
import com.keepreal.madagascar.common.ReactionType;
import com.keepreal.madagascar.fossa.dao.FeedInfoRepository;
import com.keepreal.madagascar.fossa.dao.ReactionRepository;
import com.keepreal.madagascar.fossa.model.FeedInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

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

    /**
     * Constructs the feed service
     *
     * @param mongoTemplate         {@link MongoTemplate}.
     * @param commentService        {@link CommentService}.
     * @param feedInfoRepository    {@link FeedInfoRepository}.
     * @param reactionRepository    {@link ReactionRepository}.
     */
    public FeedInfoService(MongoTemplate mongoTemplate,
                           CommentService commentService,
                           FeedInfoRepository feedInfoRepository,
                           ReactionRepository reactionRepository) {
        this.mongoTemplate = mongoTemplate;
        this.commentService = commentService;
        this.feedInfoRepository = feedInfoRepository;
        this.reactionRepository = reactionRepository;
    }

    /**
     * Delete feed by id.
     *
     * @param id    feed id.
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
     * @param feedId    feed id.
     * @param userId    user id.
     * @return  {@link FeedMessage}.
     */
    public FeedMessage getFeedMessageById(String feedId, String userId) {
        Optional<FeedInfo> feedInfoOptional = feedInfoRepository.findById(feedId);
        if (feedInfoOptional.isPresent()) {
            return getFeedMessage(feedInfoOptional.get(), userId);
        } else {
            return FeedMessage.newBuilder().build();
        }
    }

    /**
     * Feed count add one by feed id and type
     *
     * @param feedId    feed id.
     * @param type      type to be changed.
     */
    public void incFeedCount(String feedId, String type) {
        this.updateFeedCountByType(feedId, type, 1);
    }

    /**
     * Feed count sub one by feed id and type
     * todo negative count
     * @param feedId    feed id.
     * @param type      type to be changed.
     */
    public void subFeedCount(String feedId, String type) {
        this.updateFeedCountByType(feedId, type, -1);
    }

    /**
     * Change feed count by feed id and type
     *
     * @param feedId    feed id.
     * @param type      type to be changed.
     * @param count     add or sub count.
     */
    private void updateFeedCountByType(String feedId, String type, Integer count) {
        Update update = new Update();
        update.inc(type, count);
        mongoTemplate.updateFirst(Query.query(Criteria.where("id").is(feedId)), update, FeedInfo.class);
    }

    /**
     * Retrieves the feed message.
     *
     * @param feedInfo  {@link FeedInfo}.
     * @param userId    user id (decide is liked).
     * @return  {@link FeedMessage}.
     */
    public FeedMessage getFeedMessage(FeedInfo feedInfo, String userId) {
        if (feedInfo == null)
            return null;
        List<CommentMessage> lastCommentMessage = commentService.getCommentsMessage(feedInfo.getId(), DEFAULT_LAST_COMMENT_COUNT);
        boolean isLiked = reactionRepository.existsByFeedIdAndUserIdAndReactionTypeListContains(feedInfo.getId(), userId, ReactionType.REACTION_LIKE_VALUE);
        return FeedMessage.newBuilder()
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
                .setIsDeleted(feedInfo.isDeleted())
                .build();
    }

    /**
     * Inserts the feeds.
     *
     * @param feedInfoList  {@link FeedInfo}.
     */
    public List<FeedInfo> saveAll(List<FeedInfo> feedInfoList) {
        return feedInfoRepository.saveAll(feedInfoList);
    }

    /**
     * Retrieves feed by id.
     *
     * @param feedId    feed id.
     * @param includeDeleted Whether includes the deleted.
     * @return  {@link FeedInfo}.
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
     * @return  {@link FeedInfo}
     */
    public FeedInfo findTopByUserIdAndDeletedIsFalseOrderByCreatedTimeDesc(String userId) {
        return feedInfoRepository.findTopByUserIdAndDeletedIsFalseOrderByCreatedTimeDesc(userId);
    }

    /**
     * Inserts the feed.
     *
     * @param feedInfo  {@link FeedInfo}
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
        return this.feedInfoRepository.findAllByIdsAndDeletedIsFalse(ids);
    }

}
