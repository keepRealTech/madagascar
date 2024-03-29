package com.keepreal.madagascar.mantella.repository;

import com.keepreal.madagascar.mantella.model.Timeline;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Represents the timeline repository.
 */
public interface TimelineRepository extends ReactiveMongoRepository<Timeline, String> {

    Mono<Boolean> existsByEventId(String eventId);

    Flux<Timeline> findTopByUserIdAndFeedCreatedAtAfterAndIsDeletedIsFalse(String userId, long startTimestamp, Pageable pageable);

    Flux<Timeline> findTopByUserIdAndFeedCreatedAtBeforeAndIsDeletedIsFalse(String userId, long startTimestamp, Pageable pageable);

    Mono<Timeline> findTopByUserIdAndIsDeletedIsFalseOrderByFeedCreatedAtDesc(String userId);

}
