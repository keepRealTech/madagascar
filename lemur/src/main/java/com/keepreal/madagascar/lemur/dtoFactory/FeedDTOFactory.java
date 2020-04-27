package com.keepreal.madagascar.lemur.dtoFactory;

import com.keepreal.madagascar.common.FeedMessage;
import org.springframework.stereotype.Component;
import swagger.model.FeedDTO;

import java.util.Objects;

/**
 * Represents the feed dto factory.
 */
@Component
public class FeedDTOFactory {

    /**
     * Converts the {@link FeedMessage} into {@link FeedDTO}.
     *
     * @param feed {@link FeedMessage}.
     * @return {@link FeedDTO}.
     */
    public FeedDTO valueOf(FeedMessage feed) {
        if (Objects.isNull(feed)) {
            return null;
        }

        FeedDTO feedDTO = new FeedDTO();
        feedDTO.setId(feed.getId());
        feedDTO.setUserId(feed.getUserId());
        feedDTO.setIslandId(feed.getIslandId());
        feedDTO.setText(feed.getText());
        feedDTO.setImagesUris(feed.getImageUrisList());
        feedDTO.setFromHost(feed.getFromHost());
        feedDTO.setLikesCount(feedDTO.getLikesCount());
        feedDTO.setCommentsCount(feed.getCommentsCount());
        feedDTO.setRepostCount(feed.getRepostCount());
        feedDTO.setCreatedAt(feed.getCreatedAt());

        return feedDTO;
    }

}
