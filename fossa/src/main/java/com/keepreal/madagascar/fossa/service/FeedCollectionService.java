package com.keepreal.madagascar.fossa.service;

import com.keepreal.madagascar.common.snowflake.generator.LongIdGenerator;
import com.keepreal.madagascar.fossa.FeedCollectionMessage;
import com.keepreal.madagascar.fossa.dao.FeedCollectionRepository;
import com.keepreal.madagascar.fossa.model.FeedCollection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class FeedCollectionService {

    private final FeedCollectionRepository feedCollectionRepository;
    private final LongIdGenerator idGenerator;

    public FeedCollectionService(FeedCollectionRepository feedCollectionRepository,
                                 LongIdGenerator idGenerator) {
        this.feedCollectionRepository = feedCollectionRepository;
        this.idGenerator = idGenerator;
    }

    public void addFeedToCollection(String userId, String feedId) {
        FeedCollection feedCollection = this.feedCollectionRepository.findByUserIdAndFeedId(userId, feedId);
        if (feedCollection == null) {
            feedCollection = new FeedCollection();
            feedCollection.setUserId(userId);
            feedCollection.setFeedId(feedId);
            this.create(feedCollection);
        } else {
            feedCollection.setDeleted(false);
            this.update(feedCollection);
        }
    }

    public void removeFeedToCollection(String userId, String feedId) {
        FeedCollection feedCollection = this.feedCollectionRepository.findByUserIdAndFeedId(userId, feedId);
        if (feedCollection != null) {
            feedCollection.setDeleted(true);
            this.update(feedCollection);
        }
    }

    public Page<FeedCollection> findFeedCollectionsUpdatedTimeLE(String userId, long timestamp, int pageSize) {
        return this.feedCollectionRepository.findFeedCollectionsByUserIdAndDeletedIsFalseAndUpdatedTimeLessThanEqualOrderByUpdatedTimeDesc(userId, timestamp, PageRequest.of(0, pageSize));
    }

    public Page<FeedCollection> findFeedCollectionsUpdatedTimeGE(String userId, long timestamp, int pageSize) {
        return this.feedCollectionRepository.findFeedCollectionsByUserIdAndDeletedIsFalseAndUpdatedTimeGreaterThanEqualOrderByUpdatedTimeDesc(userId, timestamp, PageRequest.of(0, pageSize));
    }

    public boolean hasCollected(String userId, String feedId) {
        return this.feedCollectionRepository.existsByUserIdAndFeedIdAndDeletedIsFalse(userId, feedId);
    }

    public Set<FeedCollection> findByFeedIdsAndUserId(Iterable<String> feedIds, String userId) {
        return this.feedCollectionRepository.findByFeedIdInAndUserIdAndDeletedIsFalse(feedIds, userId);
    }

    public FeedCollectionMessage buildMessage(FeedCollection feedCollection) {
        if (feedCollection == null) {
            return FeedCollectionMessage.getDefaultInstance();
        }
        return FeedCollectionMessage.newBuilder()
                .setUserId(feedCollection.getUserId())
                .setFeedId(feedCollection.getFeedId())
                .setUpdatedTime(feedCollection.getUpdatedTime())
                .build();
    }

    private FeedCollection create(FeedCollection feedCollection) {
        feedCollection.setId(String.valueOf(idGenerator.nextId()));
        return this.feedCollectionRepository.save(feedCollection);
    }

    private FeedCollection update(FeedCollection feedCollection) {
        return this.feedCollectionRepository.save(feedCollection);
    }
}
