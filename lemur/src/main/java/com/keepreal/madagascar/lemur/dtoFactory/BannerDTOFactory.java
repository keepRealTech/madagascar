package com.keepreal.madagascar.lemur.dtoFactory;

import com.keepreal.madagascar.hoopoe.BannerMessage;
import org.springframework.stereotype.Component;
import swagger.model.BannerDTO;

import java.util.Objects;

/**
 * Represents the banner dto factory.
 */
@Component
public class BannerDTOFactory {

    /**
     * Converts the {@link BannerMessage} to the {@link BannerDTO}
     *
     * @return {@link BannerDTO}
     */
    public BannerDTO valueOf (BannerMessage bannerMessage) {
        if (Objects.isNull(bannerMessage)) {
            return null;
        }
        BannerDTO bannerDTO = new BannerDTO();
        bannerDTO.setImageUri(bannerMessage.getImageUri());
        bannerDTO.setRedirectUrl(bannerMessage.getRedirectUrl());
        return bannerDTO;
    }

}
