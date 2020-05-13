package com.keepreal.madagascar.brookesia.repository;

import com.keepreal.madagascar.brookesia.model.StatsEvent;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Represents the stats event repository.
 */
public interface StatsEventRepository extends MongoRepository<StatsEvent, String> {

}
