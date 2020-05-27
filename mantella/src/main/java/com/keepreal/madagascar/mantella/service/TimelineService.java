package com.keepreal.madagascar.mantella.service;

import com.keepreal.madagascar.mantella.FeedCreateEvent;
import com.keepreal.madagascar.mantella.model.Timeline;
import com.keepreal.madagascar.mantella.repository.TimelineRepository;
import com.keepreal.madagascar.mantella.service.distributor.FeedDistributor;
import com.keepreal.madagascar.mantella.storage.TimelineStorage;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Represents the timeline service.
 */
@Service
public class TimelineService {

    private final TimelineRepository timelineRepository;
    private final TimelineStorage timelineStorage;
    private final FeedDistributor feedDistributor;

    /**
     * Constructs the timeline service.
     *
     * @param timelineRepository {@link TimelineRepository}.
     * @param timelineStorage    {@link TimelineStorage}.
     * @param feedDistributor    {@link FeedDistributor}.
     */
    public TimelineService(TimelineRepository timelineRepository,
                           TimelineStorage timelineStorage,
                           FeedDistributor feedDistributor) {
        this.timelineRepository = timelineRepository;
        this.timelineStorage = timelineStorage;
        this.feedDistributor = feedDistributor;
    }

    /**
     * Distributes the feed event into timelines.
     *
     * @param feedCreateEvent {@link FeedCreateEvent}.
     * @return {@link Void}.
     */
    public Mono<Void> distribute(FeedCreateEvent feedCreateEvent) {
        return this.feedDistributor.distribute(feedCreateEvent)
                .compose(this::insertAll)
                .then();
    }

    /**
     * Inserts a new timeline entity.
     *
     * @param timelines {@link Timeline}.
     * @return {@link Mono}.
     */
    public Flux<Timeline> insertAll(Flux<Timeline> timelines) {
        return this.timelineRepository.insert(timelines);
    }

    /**
     * Deletes timelines by feed id.
     *
     * @param feedId Feed id.
     * @return {@link Void}.
     */
    public Mono<Void> deleteByFeedId(String feedId) {
        return this.timelineStorage.deleteByFeedId(feedId);
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