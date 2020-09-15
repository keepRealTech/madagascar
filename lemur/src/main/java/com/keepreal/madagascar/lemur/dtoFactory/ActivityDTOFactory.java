package com.keepreal.madagascar.lemur.dtoFactory;

import com.keepreal.madagascar.hoopoe.ActiveBannerMessage;
import com.keepreal.madagascar.hoopoe.SingleBannerMessage;
import org.springframework.stereotype.Component;
import swagger.model.ActivityDTO;
import swagger.model.BannerDTO;
import swagger.model.LabelDTO;

import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents the activity dto factory.
 */
@Component
public class ActivityDTOFactory {

    /**
     * Converts the     {@link ActiveBannerMessage} to {@link ActivityDTO}
     *
     * @param message   {@link ActiveBannerMessage}
     * @return          {@link ActivityDTO}
     */
    public ActivityDTO valueOf(ActiveBannerMessage message) {
        if (Objects.isNull(message)) {
            return null;
        }

        ActivityDTO activityDTO = new ActivityDTO();
        LabelDTO labelDTO = new LabelDTO();
        labelDTO.setText(message.getLabel());
        activityDTO.setLabel(labelDTO);
        activityDTO.setBanners(message.getBannersList().stream().map(this::valueOf).collect(Collectors.toList()));
        return activityDTO;
    }

    /**
     * Converts the     {@link SingleBannerMessage} to {@link BannerDTO}
     *
     * @param message   {@link SingleBannerMessage}
     * @return          {@link BannerDTO}
     */
    private BannerDTO valueOf(SingleBannerMessage message) {
        if (Objects.isNull(message)) {
            return null;
        }

        BannerDTO bannerDTO = new BannerDTO();
        bannerDTO.setImageUri(message.getImageUri());
        bannerDTO.setRedirectUrl(message.getRedirectUrl());
        return bannerDTO;
    }

}