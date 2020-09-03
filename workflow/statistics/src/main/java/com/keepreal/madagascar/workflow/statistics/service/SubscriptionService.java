package com.keepreal.madagascar.workflow.statistics.service;

import com.keepreal.madagascar.workflow.statistics.config.StatisticsConfiguration;
import com.keepreal.madagascar.workflow.statistics.model.IslandIncrement;
import com.keepreal.madagascar.workflow.statistics.repository.SubscriptionRepository;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.lang.reflect.Constructor;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the subscription service.
 */
@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final StatisticsConfiguration statisticsConfiguration;

    /**
     * Constructs the subscription service.
     *
     * @param subscriptionRepository  {@link SubscriptionRepository}.
     * @param statisticsConfiguration {@link StatisticsConfiguration}.
     */
    public SubscriptionService(SubscriptionRepository subscriptionRepository,
                               StatisticsConfiguration statisticsConfiguration) {
        this.subscriptionRepository = subscriptionRepository;
        this.statisticsConfiguration = statisticsConfiguration;
    }

    /**
     * Retrieves all islands with incremental islander count more than threshold for yesterday.
     *
     * @return Island ids.
     */
    @SneakyThrows
    public List<IslandIncrement> retrieveIslandIds() {
        long today = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long yesterday = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).minusDays(1L).toInstant().toEpochMilli();

        List<Object[]> islands = this.subscriptionRepository.findHitIslandIds(yesterday, today, this.statisticsConfiguration.getIncreIslanderThreshold());

        return this.objectTo(islands, IslandIncrement.class);
    }

    private <T> List<T> objectTo(List<Object[]> objList, Class<T> clz) throws Exception {
        if (objList == null || objList.size() == 0) {
            return null;
        }

        Class<?>[] cz = null;
        Constructor<?>[] cons = clz.getConstructors();
        for (Constructor<?> ct : cons) {
            Class<?>[] clazz = ct.getParameterTypes();
            if (objList.get(0).length == clazz.length) {
                cz = clazz;
                break;
            }
        }

        List<T> list = new ArrayList<T>();
        for (Object[] obj : objList) {
            Constructor<T> cr = clz.getConstructor(cz);
            list.add(cr.newInstance(obj));
        }
        return list;
    }
}
