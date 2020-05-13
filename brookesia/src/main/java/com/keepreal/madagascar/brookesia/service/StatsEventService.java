package com.keepreal.madagascar.brookesia.service;

import com.keepreal.madagascar.brookesia.model.StatsEvent;
import com.keepreal.madagascar.brookesia.repository.StatsEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Represents the stats event service.
 */
@Service
@Slf4j
public class StatsEventService {

    private final StatsEventRepository statsEventRepository;

    /**
     * Constructs the stats event service.
     *
     * @param statsEventRepository {@link StatsEventRepository}.
     */
    public StatsEventService(StatsEventRepository statsEventRepository) {
        this.statsEventRepository = statsEventRepository;
    }

    /**
     * Inserts a new event.
     *
     * @param event Event to insert.
     */
    public void insert(StatsEvent event) {
        this.statsEventRepository.insert(event);
    }

}
