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
import java.util.Objects;
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
        chatgroup = this.upsert(chatgroup);

        this.rongCloudService.createGroup(chatgroup.getId(), chatgroup.getName(), chatgroup.getHostId());
        return chatgroup;
    }

    /**
     * Joins the user to the group.
     *
     * @param userId    User id.
     * @param chatgroup Chat group.
     * @return {@link ChatgroupMember}.
     */
    @Transactional
    public ChatgroupMember joinChatgroup(String userId, Chatgroup chatgroup) {
        ChatgroupMember chatgroupMember = this.chatgroupMemberRepository.findByGroupIdAndUserIdAndDeletedIsFalse(chatgroup.getId(), userId);

        if (Objects.nonNull(chatgroupMember)) {
            return chatgroupMember;
        }

        chatgroupMember = ChatgroupMember.builder()
                .id(String.valueOf(this.idGenerator.nextId()))
                .userId(userId)
                .groupId(chatgroup.getId())
                .build();
        this.chatgroupMemberRepository.save(chatgroupMember);

        this.rongCloudService.joinGroup(chatgroup.getId(), userId);

        chatgroup.setMemberCount(chatgroup.getMemberCount() + 1);
        chatgroup = this.upsert(chatgroup);

        return chatgroupMember;
    }

    /**
     * Retrieves chat group by id.
     *
     * @param id             Id.
     * @param includeDeleted Whether includes the deleted chat groups.
     * @return {@link Chatgroup}.
     */
    public Chatgroup retrieveById(String id, Boolean includeDeleted) {
        if (includeDeleted) {
            return this.chatgroupRepository.findById(id).orElse(null);
        }
        return this.chatgroupRepository.findByIdAndDeletedIsFalse(id);
    }

    /**
     * Dismisses chat group by id.
     *
     * @param chatgroup {@link Chatgroup}.
     */
    public void dismiss(Chatgroup chatgroup) {
        this.rongCloudService.dismissGroup(chatgroup.getId(), chatgroup.getHostId());
        chatgroup.setDeleted(true);
        this.upsert(chatgroup);
    }

    /**
     * Updates chat group.
     *
     * @param chatgroup {@link Chatgroup}.
     * @return {@link Chatgroup}.
     */
    public Chatgroup upsert(Chatgroup chatgroup) {
        return this.chatgroupRepository.save(chatgroup);
    }

    /**
     * Updates chat group member muted.
     *
     * @param chatgroupMember {@link ChatgroupMember}.
     * @param muted           Muted.
     * @return {@link ChatgroupMember}.
     */
    public ChatgroupMember updateMutedByGroupIdAndUserId(ChatgroupMember chatgroupMember, boolean muted) {
        if (Objects.isNull(chatgroupMember)) {
            return null;
        }

        chatgroupMember.setMuted(muted);

        return this.chatgroupMemberRepository.save(chatgroupMember);
    }

    /**
     * Retrieves the chat group member.
     *
     * @param groupId Group id.
     * @param userId  User id.
     * @return {@link ChatgroupMember}.
     */
    public ChatgroupMember retrieveChatgroupMemberByGroupIdAndUserId(String groupId, String userId) {
        return this.chatgroupMemberRepository.findByGroupIdAndUserIdAndDeletedIsFalse(groupId, userId);
    }

    /**
     * Resets the chatgroup memberships.
     *
     * @param chatgroup     {@link Chatgroup}.
     * @param membershipIds Membership ids.
     * @return {@link Chatgroup}.
     */
    public Chatgroup updateChatgroupMembershipInMem(Chatgroup chatgroup, List<String> membershipIds) {
        Set<ChatgroupMembership> membershipSet = membershipIds.stream()
                .map(id -> ChatgroupMembership.builder()
                        .id(String.valueOf(this.idGenerator.nextId()))
                        .groupId(chatgroup.getId())
                        .membershipId(id)
                        .build())
                .collect(Collectors.toSet());

        chatgroup.setChatgroupMemberships(membershipSet);
        return chatgroup;
    }

    /**
     * Counts the chatgroups by island.
     *
     * @param islandId Island id.
     * @return Counts.
     */
    public Integer countChatgroupsByIslandId(String islandId) {
        return Math.toIntExact(this.chatgroupRepository.countByIslandIdAndDeletedIsFalse(islandId));
    }

    /**
     * Quits all chatgroups by island.
     *
     * @param islandId Island id.
     * @param userId   User id.
     */
    public void quitChatgroupsByIslandId(String islandId, String userId) {
        List<ChatgroupMember> chatgroupMembers =
                this.chatgroupMemberRepository.findAllByIslandIdAndUserIdAndDeletedIsFalse(islandId, userId);

        if (chatgroupMembers.isEmpty()) {
            return;
        }

        chatgroupMembers = chatgroupMembers.stream()
                .peek(chatgroupMember -> chatgroupMember.setDeleted(true))
                .collect(Collectors.toList());

        this.chatgroupMemberRepository.saveAll(chatgroupMembers);
    }

}
