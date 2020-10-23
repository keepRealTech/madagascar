package com.keepreal.madagascar.fossa.dao;

import com.keepreal.madagascar.fossa.model.FeedGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Represents the feed group repository.
 */
public interface FeedGroupRepository extends MongoRepository<FeedGroup, String> {

    FeedGroup findByIdAndDeletedIsFalse(String id);

    List<FeedGroup> findAllByIdInAndDeletedIsFalse(Iterable<String> ids);

    Page<FeedGroup> findAllByIslandIdAndDeletedIsFalseOrderByLastFeedTimeDesc(String islandId, Pageable pageable);

    Boolean existsByHostIdAndDeletedIsFalse(String userId);

}
