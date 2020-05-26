package com.keepreal.madagascar.mantella.repository;

import com.keepreal.madagascar.mantella.model.Timeline;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface TimelineRepository extends ReactiveMongoRepository<Timeline, String> {

}
