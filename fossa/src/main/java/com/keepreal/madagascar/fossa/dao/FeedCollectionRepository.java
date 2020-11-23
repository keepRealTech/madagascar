package com.keepreal.madagascar.fossa.dao;

import com.keepreal.madagascar.fossa.model.FeedCollection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface FeedCollectionRepository extends MongoRepository<FeedCollection, String> {

    FeedCollection findByUserIdAndFeedId(String userId, String feedId);

    Page<FeedCollection> findFeedCollectionsByUserIdAndDeletedIsFalseAndUpdatedTimeGreaterThanOrderByUpdatedTimeDesc(String userId, Long timestamp, Pageable pageable);

    Page<FeedCollection> findFeedCollectionsByUserIdAndDeletedIsFalseAndUpdatedTimeLessThanOrderByUpdatedTimeDesc(String userId, Long timestamp, Pageable pageable);

    boolean existsByUserIdAndFeedIdAndDeletedIsFalse(String userId, String feedId);

    Set<FeedCollection> findByFeedIdInAndUserIdAndDeletedIsFalse(Iterable<String> feedIds, String userId);
}
