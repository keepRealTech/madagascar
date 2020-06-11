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

    Flux<Timeline> findTopByUserIdAndFeedCreatedAtAfterAndIsDeletedIsTrue(String userId, long startTimestamp, Pageable pageable);

    Mono<Timeline> findTopByUserIdAndIsDeleteIsFalseOrderByFeedCreatedAtDesc(String userId);

}
