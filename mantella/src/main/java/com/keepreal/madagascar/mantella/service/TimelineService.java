package com.keepreal.madagascar.mantella.service;

import com.keepreal.madagascar.mantella.FeedCreateEvent;
import com.keepreal.madagascar.mantella.FeedEvent;
import com.keepreal.madagascar.mantella.FeedEventMessage;
import com.keepreal.madagascar.mantella.factory.TimelineFactory;
import com.keepreal.madagascar.mantella.model.Timeline;
import com.keepreal.madagascar.mantella.repository.TimelineRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class TimelineService {

    private final TimelineRepository timelineRepository;
    private final TimelineService timelineService;
    private final TimelineFactory timelineFactory;

    public TimelineService(TimelineRepository timelineRepository,
                           TimelineService timelineService,
                           TimelineFactory timelineFactory) {
        this.timelineRepository = timelineRepository;
        this.timelineService = timelineService;
        this.timelineFactory = timelineFactory;
    }

    /**
     * Distributes the feed event into timelines.
     *
     * @param feedCreateEvent {@link FeedCreateEvent}.
     * @return {@link Void}.
     */
    public Mono<Void> distribute(FeedCreateEvent feedCreateEvent) {
        Timeline timeline = this.timelineFactory.valueOf(feedCreateEvent);


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
