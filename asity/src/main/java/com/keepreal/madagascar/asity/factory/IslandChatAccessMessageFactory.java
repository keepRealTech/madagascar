package com.keepreal.madagascar.asity.factory;

import com.keepreal.madagascar.asity.ChatAccessMessage;
import com.keepreal.madagascar.asity.model.IslandChatAccess;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Represents the island chat access message factory.
 */
@Component
public class IslandChatAccessMessageFactory {

    /**
     * Converts {@link IslandChatAccess} into {@link ChatAccessMessage}.
     *
     * @param islandChatAccess {@link IslandChatAccess}.
     * @return {@link ChatAccessMessage}.
     */
    public ChatAccessMessage valueOf(IslandChatAccess islandChatAccess) {
        if (Objects.isNull(islandChatAccess)) {
            return ChatAccessMessage.getDefaultInstance();
        }

        return ChatAccessMessage.newBuilder()
                .setId(islandChatAccess.getId())
                .setIslandId(islandChatAccess.getIslandId())
                .setHasAccess(islandChatAccess.getEnabled())
                .build();
    }

}
