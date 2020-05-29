package com.keepreal.madagascar.mantella.service.distributor;

import com.keepreal.madagascar.mantella.FeedCreateEvent;
import com.keepreal.madagascar.mantella.model.Timeline;
import reactor.core.publisher.Flux;

/**
 * Represents the feed distributor interface.
 */
public interface FeedDistributor {

    Flux<Timeline> distribute(FeedCreateEvent feedCreateEvent, String eventId);

}
