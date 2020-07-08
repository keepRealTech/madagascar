package com.keepreal.madagascar.asity.service;

import com.keepreal.madagascar.asity.model.Chatgroup;
import com.keepreal.madagascar.asity.model.ChatgroupMember;
import com.keepreal.madagascar.asity.model.ChatgroupMembership;
import com.keepreal.madagascar.asity.repository.ChatgroupMemberRepository;
import com.keepreal.madagascar.asity.repository.ChatgroupRepository;
import com.keepreal.madagascar.common.snowflake.generator.LongIdGenerator;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents the chatgroup service.
 */
@Service
public class ChatgroupService {

    private final ChatgroupRepository chatgroupRepository;
    private final ChatgroupMemberRepository chatgroupMemberRepository;
    private final RongCloudService rongCloudService;
    private final LongIdGenerator idGenerator;

    public ChatgroupService(ChatgroupRepository chatgroupRepository,
                            ChatgroupMemberRepository chatgroupMemberRepository,
                            RongCloudService rongCloudService,
                            LongIdGenerator idGenerator) {
        this.chatgroupRepository = chatgroupRepository;
        this.chatgroupMemberRepository = chatgroupMemberRepository;
        this.rongCloudService = rongCloudService;
        this.idGenerator = idGenerator;
    }

    /**
     * Creates a chatgroup.
     *
     * @param islandId      Island id.
     * @param name          Name.
     * @param hostId        Host id.
     * @param membershipIds Membership ids.
     * @param bulletin      Bulletin.
     * @return {@link Chatgroup}.
     */
    @Transactional
    public Chatgroup createChatgroup(String islandId, String name, String hostId, List<String> membershipIds, String bulletin) {
        String groupId = String.valueOf(this.idGenerator.nextId());

        Set<ChatgroupMembership> membershipSet = membershipIds.stream()
                .map(id -> ChatgroupMembership.builder()
                        .id(String.valueOf(this.idGenerator.nextId()))
                        .groupId(groupId)
                        .membershipId(id)
                        .build())
                .collect(Collectors.toSet());

        Chatgroup chatgroup = Chatgroup.builder()
                .id(groupId)
                .islandId(islandId)
                .name(name)
                .hostId(hostId)
                .chatgroupMemberships(membershipSet)
                .bulletin(bulletin)
                .build();
        chatgroup = this.chatgroupRepository.save(chatgroup);

        this.rongCloudService.createGroup(chatgroup.getId(), chatgroup.getName(), chatgroup.getHostId());
        return chatgroup;
    }

    /**
     * Joins the user to the group.
     *
     * @param userId  User id.
     * @param groupId Group id.
     * @return {@link ChatgroupMember}.
     */
    @Transactional
    public ChatgroupMember joinChatgroup(String userId, String groupId) {
        ChatgroupMember chatgroupMember = ChatgroupMember.builder()
                .id(String.valueOf(this.idGenerator.nextId()))
                .userId(userId)
                .groupId(groupId)
                .build();
        this.chatgroupMemberRepository.save(chatgroupMember);

        this.rongCloudService.joinGroup(groupId, userId);

        return chatgroupMember;
    }

}
