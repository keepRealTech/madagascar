package com.keepreal.madagascar.mantella.repository;

import com.keepreal.madagascar.mantella.FeedCreateEvent;
import com.keepreal.madagascar.mantella.model.Timeline;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

/**
 * Represents the timeline repository.
 */
public interface TimelineRepository extends ReactiveMongoRepository<Timeline, String> {

    Mono<Boolean> existsByFeedId(String feedId);

}
