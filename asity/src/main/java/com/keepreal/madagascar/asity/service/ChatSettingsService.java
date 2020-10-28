package com.keepreal.madagascar.asity.service;

import com.keepreal.madagascar.asity.model.ChatSettings;
import com.keepreal.madagascar.asity.repository.ChatSettingsRepository;
import com.keepreal.madagascar.common.snowflake.generator.LongIdGenerator;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Represents the chat settings service.
 */
@Service
public class ChatSettingsService {

    private final LongIdGenerator idGenerator;
    private final ChatSettingsRepository chatSettingsRepository;

    /**
     * Constructs the chat settings service.
     *
     * @param idGenerator            {@link LongIdGenerator}.
     * @param chatSettingsRepository {@link ChatSettingsRepository}.
     */
    public ChatSettingsService(LongIdGenerator idGenerator,
                               ChatSettingsRepository chatSettingsRepository) {
        this.idGenerator = idGenerator;
        this.chatSettingsRepository = chatSettingsRepository;
    }

    /**
     * Retrieves or creates if not exists for given user.
     *
     * @param userId User id.
     * @return {@link ChatSettings}.
     */
    public ChatSettings retrieveOrCreateChatSettingsIfNotExistsByUserId(String userId) {
        ChatSettings chatSettings = this.chatSettingsRepository.findByUserIdAndDeletedIsFalse(userId);
        if (Objects.nonNull(chatSettings)) {
            return chatSettings;
        }

        try {
            return this.createChatSettings(userId);
        } catch (DuplicateKeyException exception) {
            return this.chatSettingsRepository.findByUserIdAndDeletedIsFalse(userId);
        }
    }

    /**
     * Updates the chat settings.
     *
     * @param chatSettings {@link ChatSettings}.
     * @return {@link ChatSettings}.
     */
    public ChatSettings update(ChatSettings chatSettings) {
        return this.chatSettingsRepository.save(chatSettings);
    }

    /**
     * Creates a new entity for given user.
     *
     * @param userId User id.
     * @return {@link ChatSettings}.
     */
    private ChatSettings createChatSettings(String userId) {
        ChatSettings chatSettings = ChatSettings.builder()
                .id(String.valueOf(this.idGenerator.nextId()))
                .userId(userId)
                .displayPaymentMessage(true)
                .build();

        return this.update(chatSettings);
    }

}
