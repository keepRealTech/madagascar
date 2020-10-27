package com.keepreal.madagascar.asity.factory;

import com.keepreal.madagascar.asity.ChatgroupMessage;
import com.keepreal.madagascar.asity.model.Chatgroup;
import com.keepreal.madagascar.asity.model.ChatgroupMember;
import com.keepreal.madagascar.asity.model.ChatgroupMembership;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents the chat group message factory.
 */
@Component
public class ChatgroupMessageFactory {

    /**
     * Generates the chatgroup message.
     *
     * @param chatgroup       {@link Chatgroup}.
     * @param chatgroupMember {@link ChatgroupMember}.
     * @return {@link ChatgroupMessage}.
     */
    public ChatgroupMessage valueOf(Chatgroup chatgroup, ChatgroupMember chatgroupMember) {
        if (Objects.isNull(chatgroup)) {
            return ChatgroupMessage.getDefaultInstance();
        }

        return ChatgroupMessage.newBuilder()
                .setId(chatgroup.getId())
                .setIslandId(chatgroup.getIslandId())
                .setHostId(chatgroup.getHostId())
                .setName(chatgroup.getName())
                .setBulletin(chatgroup.getBulletin())
                .setMemberCount(chatgroup.getMemberCount())
                .setJoined(!Objects.isNull(chatgroupMember))
                .addAllMembershipIds(chatgroup.getChatgroupMemberships().stream()
                        .map(ChatgroupMembership::getMembershipId)
                        .collect(Collectors.toList()))
                .setMuted(Objects.isNull(chatgroupMember) ? false : chatgroupMember.getMuted())
                .build();
    }

}
