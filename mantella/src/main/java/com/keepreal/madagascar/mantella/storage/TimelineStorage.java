package com.keepreal.madagascar.mantella.storage;

import com.keepreal.madagascar.mantella.model.Timeline;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Represents some customized mongo operations.
 */
@Service
public class TimelineStorage {

    private final ReactiveMongoTemplate reactiveMongoTemplate;

    /**
     * Constructs the timeline storage.
     *
     * @param reactiveMongoTemplate {@link ReactiveMongoTemplate}.
     */
    public TimelineStorage(ReactiveMongoTemplate reactiveMongoTemplate) {
        this.reactiveMongoTemplate = reactiveMongoTemplate;
    }

    /**
     * Deletes timeline by feed id.
     *
     * @param feedId Feed id.
     * @return Void.
     */
    public Mono<Void> deleteByFeedId(String feedId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("feedId").is(feedId));
        Update update = new Update()
                .set("isDeleted", true)
                .set("updatedAt", System.currentTimeMillis());
        return this.reactiveMongoTemplate.updateMulti(query, update, Timeline.class)
                .then();
    }

}
