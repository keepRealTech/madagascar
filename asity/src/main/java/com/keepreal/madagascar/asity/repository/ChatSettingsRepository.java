package com.keepreal.madagascar.asity.repository;

import com.keepreal.madagascar.asity.model.ChatSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Represents the chat settings repository.
 */
@Repository
public interface ChatSettingsRepository extends JpaRepository<ChatSettings, String> {

    ChatSettings findByUserIdAndDeletedIsFalse(String userId);

}
