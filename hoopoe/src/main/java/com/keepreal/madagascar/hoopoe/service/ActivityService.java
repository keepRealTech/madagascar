package com.keepreal.madagascar.hoopoe.service;

import com.keepreal.madagascar.hoopoe.dao.ActivityRepository;
import com.keepreal.madagascar.hoopoe.model.Activity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ActivityService {

    private final ActivityRepository activityRepository;

    public ActivityService(ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    /**
     * 获取可用的活动banner
     *
     * @return  {@link List<Activity>}
     */
    public List<Activity> findAllAccessActivities() {
        return this.activityRepository.findAllByActiveIsTrueAndDeletedIsFalse();
    }

}