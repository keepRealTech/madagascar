package com.keepreal.madagascar.mantella.service;

import com.keepreal.madagascar.mantella.FeedEventMessage;
import com.keepreal.madagascar.mantella.model.Timeline;
import com.keepreal.madagascar.mantella.repository.TimelineRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class TimelineService {

    private final TimelineRepository timelineRepository;

    public TimelineService(TimelineRepository timelineRepository) {
        this.timelineRepository = timelineRepository;
    }

    public Mono<Void> distribute(FeedEventMessage feedEventMessage) {
        return Mono.empty();
    }

    public Mono<Timeline> insert(Timeline timeline) {
        return this.timelineRepository.insert(timeline);
    }

    public Mono<Void> delete(String feedId) {
        return Mono.empty();
    }

    /**
     * Checks if the feed event has consumed.
     *
     * @param feedId Feed id.
     * @return True if has been consumed.
     */
    public Mono<Boolean> hasConsumed(String feedId) {
        return this.timelineRepository.existsByFeedId(feedId);
    }

}
