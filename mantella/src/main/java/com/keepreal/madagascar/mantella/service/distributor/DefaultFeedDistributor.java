package com.keepreal.madagascar.mantella.service.distributor;

import com.keepreal.madagascar.mantella.FeedCreateEvent;
import com.keepreal.madagascar.mantella.factory.TimelineFactory;
import com.keepreal.madagascar.mantella.model.Timeline;
import com.keepreal.madagascar.mantella.service.IslandService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Represents a default implementation for feed distributor.
 */
@Component(value = "default")
@Primary
public class DefaultFeedDistributor implements FeedDistributor {

    private final TimelineFactory timelineFactory;
    private final IslandService islandService;
    private static final String PUBLIC_INBOX_USER_ID = "00000000";

    /**
     * Constructs the default feed distributor.
     *
     * @param timelineFactory {@link TimelineFactory}.
     * @param islandService   {@link IslandService}.
     */
    public DefaultFeedDistributor(TimelineFactory timelineFactory,
                                  IslandService islandService) {
        this.timelineFactory = timelineFactory;
        this.islandService = islandService;
    }

    /**
     * Implements the distribute logic for the default distributor.
     *
     * @param feedCreateEvent {@link FeedCreateEvent}.
     * @return A list of {@link Timeline}.
     */
    @Override
    public Flux<Timeline> distribute(FeedCreateEvent feedCreateEvent, String eventId) {
        Flux<Timeline> subscriberFlux = this.islandService.retrieveSubscriberIdsByIslandId(feedCreateEvent.getIslandId())
                .map(userId -> this.timelineFactory.valueOf(feedCreateEvent, userId, eventId));

        Mono<Timeline> publicMono = this.islandService.checkIslandAccessTypeIsPublic(feedCreateEvent.getIslandId())
                .filter(Boolean.TRUE::equals)
                .map(signal -> this.timelineFactory.valueOf(feedCreateEvent, DefaultFeedDistributor.PUBLIC_INBOX_USER_ID, eventId));

        return subscriberFlux.mergeWith(publicMono);
    }

}
