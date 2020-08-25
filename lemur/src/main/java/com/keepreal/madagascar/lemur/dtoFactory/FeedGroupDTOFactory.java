package com.keepreal.madagascar.lemur.dtoFactory;

import com.keepreal.madagascar.common.FeedGroupMessage;
import com.keepreal.madagascar.lemur.service.UserService;
import org.springframework.stereotype.Component;
import swagger.model.FeedGroupDTO;

import java.util.Objects;

/**
 * Represents the feed group dto factory.
 */
@Component
public class FeedGroupDTOFactory {

    private final UserService userService;
    private final UserDTOFactory userDTOFactory;

    /**
     * Constructs the feed group dto factory.
     *
     * @param userService       {@link UserService}.
     * @param userDTOFactory    {@link UserDTOFactory}.
     */
    public FeedGroupDTOFactory(UserService userService,
                               UserDTOFactory userDTOFactory) {
        this.userService = userService;
        this.userDTOFactory = userDTOFactory;
    }

    /**
     * Converts the {@link FeedGroupMessage} into {@link FeedGroupDTO}.
     *
     * @param feedGroupMessage {@link FeedGroupMessage}.
     * @return {@link FeedGroupDTO}.
     */
    public FeedGroupDTO valueOf(FeedGroupMessage feedGroupMessage) {
        if (Objects.isNull(feedGroupMessage)) {
            return null;
        }

        FeedGroupDTO feedGroupDTO = new FeedGroupDTO();
        feedGroupDTO.setId(feedGroupMessage.getId());
        feedGroupDTO.setIslandId(feedGroupMessage.getIslandId());
        feedGroupDTO.setItemCount(feedGroupMessage.getItemsCount());
        feedGroupDTO.setName(feedGroupMessage.getName());
        feedGroupDTO.setLastFeedTimestamp(feedGroupMessage.getLastFeedTime());
        feedGroupDTO.setThumbnailUri(feedGroupMessage.getThumbnailUri());
        feedGroupDTO.setImageUris(feedGroupMessage.getImageUrisList());
        feedGroupDTO.setDescription(feedGroupMessage.getDescription());

        feedGroupDTO.setHost(this.userDTOFactory.briefValueOf(this.userService.retrieveUserById(feedGroupMessage.getUserId())));

        return feedGroupDTO;
    }

}
