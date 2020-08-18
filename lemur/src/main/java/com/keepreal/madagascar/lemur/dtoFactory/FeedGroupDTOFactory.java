package com.keepreal.madagascar.lemur.dtoFactory;

import com.keepreal.madagascar.fossa.FeedGroupMessage;
import org.springframework.stereotype.Component;
import swagger.model.FeedGroupDTO;

import java.util.Objects;

/**
 * Represents the feed group dto factory.
 */
@Component
public class FeedGroupDTOFactory {

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
        feedGroupDTO.setHostId(feedGroupMessage.getUserId());
        feedGroupDTO.setIslandId(feedGroupMessage.getIslandId());
        feedGroupDTO.setItemCount(feedGroupMessage.getItemsCount());
        feedGroupDTO.setName(feedGroupMessage.getName());
        feedGroupDTO.setUpdatedAt(feedGroupMessage.getLastFeedTime());
        feedGroupDTO.setThumbnailUri(feedGroupMessage.getThumbnailUri());
        feedGroupDTO.setImageUris(feedGroupMessage.getImageUrisList());
        feedGroupDTO.setDescription(feedGroupMessage.getDescription());
        return feedGroupDTO;
    }

}
