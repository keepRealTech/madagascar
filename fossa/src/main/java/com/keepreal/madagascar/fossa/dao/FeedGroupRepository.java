package com.keepreal.madagascar.fossa.dao;

import com.keepreal.madagascar.fossa.model.FeedGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Represents the feed group repository.
 */
public interface FeedGroupRepository extends MongoRepository<FeedGroup, String> {

    FeedGroup findByIdAndDeletedIsFalse(String id);

    Page<FeedGroup> findAllByIslandIdAndDeletedIsFalse(String islandId, Pageable pageable);

}
