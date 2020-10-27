package com.keepreal.madagascar.asity.factory;

import com.keepreal.madagascar.asity.ChatSettingsMessage;
import com.keepreal.madagascar.asity.model.ChatSettings;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Represents the chat settings message factory.
 */
@Component
public class ChatSettingsMessageFactory {

    /**
     * Converts {@link ChatSettings} into grpc {@link ChatSettingsMessage}.
     *
     * @param chatSettings {@link ChatSettings}.
     * @return {@link ChatSettingsMessage}.
     */
    public ChatSettingsMessage valueOf(ChatSettings chatSettings) {
        if (Objects.isNull(chatSettings)) {
            return ChatSettingsMessage.getDefaultInstance();
        }

        return ChatSettingsMessage.newBuilder()
                .setId(chatSettings.getId())
                .setUserId(chatSettings.getUserId())
                .setDisplayPaymentMessage(chatSettings.getDisplayPaymentMessage())
                .build();
    }

}
