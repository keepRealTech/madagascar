package com.keepreal.madagascar.hoopoe.service;

import com.keepreal.madagascar.hoopoe.BannerMessage;
import com.keepreal.madagascar.hoopoe.dao.ActivityRepository;
import com.keepreal.madagascar.hoopoe.model.Activity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * Represents the banner service.
 */
@Slf4j
@Service
public class BannerService {

    private final ActivityRepository activityRepository;

    /**
     * Constructs the banner service
     *
     * @param activityRepository {@link ActivityRepository}
     */
    public BannerService(ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    /**
     * retrieve banners
     *
     * @return {@link List<Activity>}
     */
    public List<Activity> findBannersByType(int bannerType) {
        return this.activityRepository.findAllByTypeAndActiveIsTrueAndDeletedIsFalse(bannerType);
    }

    /**
     * Converts the {@link Activity} to the {@link BannerMessage}.
     *
     * @param activity {@link Activity}
     * @return {@link BannerMessage}
     */
    public BannerMessage getBannerMessage(Activity activity) {
        if (Objects.isNull(activity)) {
            return null;
        }

        return BannerMessage.newBuilder()
                .setImageUri(activity.getImageUri())
                .setRedirectUrl(activity.getRedirectUrl())
                .build();
    }

}