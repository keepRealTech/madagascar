package com.keepreal.madagascar.asity.grpcController;

import com.keepreal.madagascar.asity.ChatServiceGrpc;
import com.keepreal.madagascar.asity.ChatgroupMembersResponse;
import com.keepreal.madagascar.asity.ChatgroupResponse;
import com.keepreal.madagascar.asity.CreateChatgroupRequest;
import com.keepreal.madagascar.asity.DismissChatgroupRequest;
import com.keepreal.madagascar.asity.EnableChatAccessRequest;
import com.keepreal.madagascar.asity.IslandChatAccessResponse;
import com.keepreal.madagascar.asity.IslandChatgroupsResponse;
import com.keepreal.madagascar.asity.JoinChatgroupRequest;
import com.keepreal.madagascar.asity.RegisterRequest;
import com.keepreal.madagascar.asity.RegisterResponse;
import com.keepreal.madagascar.asity.RetreiveChatgroupsByUserIdRequest;
import com.keepreal.madagascar.asity.RetrieveChatAccessByIslandIdRequest;
import com.keepreal.madagascar.asity.RetrieveChatgroupByIdRequest;
import com.keepreal.madagascar.asity.RetrieveChatgroupMembersByGroupIdRequest;
import com.keepreal.madagascar.asity.RetrieveChatgroupsByIslandIdRequest;
import com.keepreal.madagascar.asity.UpdateChatgroupRequest;
import com.keepreal.madagascar.asity.UserChatgroupsResponse;
import com.keepreal.madagascar.asity.factory.ChatgroupMessageFactory;
import com.keepreal.madagascar.asity.factory.IslandChatAccessMessageFactory;
import com.keepreal.madagascar.asity.model.Chatgroup;
import com.keepreal.madagascar.asity.model.ChatgroupMember;
import com.keepreal.madagascar.asity.model.ChatgroupMembership;
import com.keepreal.madagascar.asity.model.IslandChatAccess;
import com.keepreal.madagascar.asity.service.ChatEventProducerService;
import com.keepreal.madagascar.asity.service.ChatgroupService;
import com.keepreal.madagascar.asity.service.IslandChatAccessService;
import com.keepreal.madagascar.asity.service.RongCloudService;
import com.keepreal.madagascar.asity.util.CommonStatusUtils;
import com.keepreal.madagascar.asity.util.PaginationUtils;
import com.keepreal.madagascar.common.CommonStatus;
import com.keepreal.madagascar.common.PageRequest;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.data.domain.Page;

import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Represents the chat grpc service.
 */
@GRpcService
public class ChatController extends ChatServiceGrpc.ChatServiceImplBase {

    private static final int GROUP_MAX_MEMBER_LIMIT = 3000;

    private final ChatgroupService chatgroupService;
    private final RongCloudService rongCloudService;
    private final IslandChatAccessService islandChatAccessService;
    private final ChatEventProducerService chatEventProducerService;

    private final ChatgroupMessageFactory chatgroupMessageFactory;
    private final IslandChatAccessMessageFactory islandChatAccessMessageFactory;

    /**
     * Constructs the chat controller.
     *
     * @param chatgroupService               {@link ChatgroupService}.
     * @param rongCloudService               {@link RongCloudService}.
     * @param islandChatAccessService        {@link IslandChatAccessService}.
     * @param chatEventProducerService       {@link ChatEventProducerService}.
     * @param chatgroupMessageFactory        {@link IslandChatAccessService}.
     * @param islandChatAccessMessageFactory {@link IslandChatAccessMessageFactory}.
     */
    public ChatController(ChatgroupService chatgroupService,
                          RongCloudService rongCloudService,
                          IslandChatAccessService islandChatAccessService,
                          ChatEventProducerService chatEventProducerService,
                          ChatgroupMessageFactory chatgroupMessageFactory,
                          IslandChatAccessMessageFactory islandChatAccessMessageFactory) {
        this.chatgroupService = chatgroupService;
        this.rongCloudService = rongCloudService;
        this.islandChatAccessService = islandChatAccessService;
        this.chatEventProducerService = chatEventProducerService;
        this.chatgroupMessageFactory = chatgroupMessageFactory;
        this.islandChatAccessMessageFactory = islandChatAccessMessageFactory;
    }

    /**
     * Registers a user.
     *
     * @param request          {@link RegisterRequest}.
     * @param responseObserver {@link StreamObserver}.
     */
    @Override
    public void registerUser(RegisterRequest request,
                             StreamObserver<RegisterResponse> responseObserver) {
        RegisterResponse response;
        try {
            String token = this.rongCloudService.register(request.getUserId(), request.getUserName(), request.getPortraitUrl());
            response = RegisterResponse.newBuilder()
                    .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC))
                    .setToken(token)
                    .build();
        } catch (KeepRealBusinessException e) {
            response = RegisterResponse.newBuilder()
                    .setStatus(CommonStatusUtils.buildCommonStatus(e.getErrorCode()))
                    .build();
        } catch (Exception e) {
            response = RegisterResponse.newBuilder()
                    .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_UNEXPECTED_ERROR))
                    .build();
        }

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * Enables the island chat access.
     *
     * @param request          {@link EnableChatAccessRequest}.
     * @param responseObserver {@link StreamObserver}.
     */
    @Override
    public void enableChatAccess(EnableChatAccessRequest request,
                                 StreamObserver<CommonStatus> responseObserver) {
        this.islandChatAccessService.enable(request.getIslandId());

        CommonStatus response = CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * Retrieves the chat access for an island and user.
     *
     * @param request          {@link RetrieveChatAccessByIslandIdRequest}.
     * @param responseObserver {@link StreamObserver}.
     */
    @Override
    public void retrieveChatAccessByIslandId(RetrieveChatAccessByIslandIdRequest request,
                                             StreamObserver<IslandChatAccessResponse> responseObserver) {
        IslandChatAccess islandChatAccess = this.islandChatAccessService.
                retrieveOrCreateIslandChatAccessIfNotExistsByIslandId(request.getIslandId());

        IslandChatAccessResponse response = IslandChatAccessResponse.newBuilder()
                .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC))
                .setChatAccess(this.islandChatAccessMessageFactory.valueOf(islandChatAccess))
                .setEnabledMemberCount(this.islandChatAccessService.countEnabledMember(request.getIslandId()))
                .setIslandChatGroupCount(this.chatgroupService.countChatgroupsByIslandId(request.getIslandId()))
                .addAllRecentEnabledUserIds(this.islandChatAccessService.retrieveLastEnabledUserIds(request.getIslandId()))
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * Creates a chat group.
     *
     * @param request          {@link CreateChatgroupRequest}.
     * @param responseObserver {@link StreamObserver}.
     */
    @Transactional
    @Override
    public void createChatgroup(CreateChatgroupRequest request,
                                StreamObserver<ChatgroupResponse> responseObserver) {
        Chatgroup chatgroup = this.chatgroupService.createChatgroup(request.getIslandId(), request.getName(), request.getHostId(),
                request.getMembershipIdsList(), request.getBulletin());

        ChatgroupMember chatgroupMember = this.chatgroupService.joinChatgroup(request.getHostId(), chatgroup);

        ChatgroupResponse response = ChatgroupResponse.newBuilder()
                .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC))
                .setChatgroup(this.chatgroupMessageFactory.valueOf(chatgroup, chatgroupMember))
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * Dismisses a chat group.
     *
     * @param request          {@link DismissChatgroupRequest}.
     * @param responseObserver {@link CommonStatus}.
     */
    @Override
    public void dismissChatgroup(DismissChatgroupRequest request,
                                 StreamObserver<CommonStatus> responseObserver) {
        Chatgroup chatgroup = this.chatgroupService.retrieveById(request.getId(), false);

        CommonStatus response;
        if (Objects.isNull(chatgroup)) {
            response = CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_CHATGROUP_NOT_FOUND_ERROR);
        } else if (!chatgroup.getHostId().equals(request.getUserId())) {
            response = CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_FORBIDDEN);
        } else {
            this.chatgroupService.dismiss(chatgroup);
            response = CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC);
        }

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * Updates chat group.
     *
     * @param request          {@link UpdateChatgroupRequest}.
     * @param responseObserver {@link ChatgroupResponse}.
     */
    @Override
    public void updateChatgroup(UpdateChatgroupRequest request,
                                StreamObserver<ChatgroupResponse> responseObserver) {
        Chatgroup chatgroup = this.chatgroupService.retrieveById(request.getId(), false);

        ChatgroupResponse response;
        if (Objects.isNull(chatgroup)) {
            response = ChatgroupResponse.newBuilder()
                    .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_CHATGROUP_NOT_FOUND_ERROR))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
            return;
        }

        ChatgroupMember chatgroupMember = this.chatgroupService.retrieveChatgroupMemberByGroupIdAndUserId(chatgroup.getId(), request.getUserId());
        if (Objects.isNull(chatgroupMember)) {
            response = ChatgroupResponse.newBuilder()
                    .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_CHATGROUP_NOT_MEMBER_ERROR))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
            return;
        }

        Set<String> newMembershipIds = new HashSet<>(request.getMembershipIdsList());
        Set<String> currentMembershipIds = chatgroup.getChatgroupMemberships().stream().map(ChatgroupMembership::getMembershipId).collect(Collectors.toSet());

        if ((request.hasName() || request.hasBulletin() || !newMembershipIds.equals(currentMembershipIds))
                && !chatgroup.getHostId().equals(request.getUserId())) {
            response = ChatgroupResponse.newBuilder()
                    .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_FORBIDDEN))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
            return;
        }

        if (request.hasMuted()) {
            chatgroupMember = this.chatgroupService.updateMutedByGroupIdAndUserId(chatgroupMember, request.getMuted().getValue());
        }

        if (request.hasName()) {
            chatgroup.setName(request.getName().getValue());
        }

        if (request.hasBulletin()) {
            String bulletin = request.getBulletin().getValue();
            chatgroup.setBulletin(bulletin);
            chatEventProducerService.produceUpdateBulletinChatEventAsync(chatgroup.getId(), request.getUserId(), bulletin);
        }

        if (Objects.nonNull(request.getMembershipIdsList())) {
            chatgroup = this.chatgroupService.updateChatgroupMembershipInMem(chatgroup, request.getMembershipIdsList());
        }

        chatgroup = this.chatgroupService.upsert(chatgroup);

        response = ChatgroupResponse.newBuilder()
                .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC))
                .setChatgroup(this.chatgroupMessageFactory.valueOf(chatgroup, chatgroupMember))
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * Implements the join group.
     *
     * @param request          {@link JoinChatgroupRequest}.
     * @param responseObserver {@link StreamObserver}.
     */
    @Override
    public void joinChatgroup(JoinChatgroupRequest request,
                              StreamObserver<CommonStatus> responseObserver) {
        Chatgroup chatgroup = this.chatgroupService.retrieveById(request.getChatgroupId(), false);

        CommonStatus response;
        if (chatgroup.getMemberCount() >= ChatController.GROUP_MAX_MEMBER_LIMIT) {
            response = CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_CHATGROUP_MAX_MEMBER_REACHED_ERROR);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            return;
        }

        this.chatgroupService.joinChatgroup(request.getUserId(), chatgroup);
        response = CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC);

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * Implements the get chatgroup by id.
     *
     * @param request          {@link RetrieveChatgroupByIdRequest}.
     * @param responseObserver {@link StreamObserver}.
     */
    @Override
    public void retrieveChatgroupById(RetrieveChatgroupByIdRequest request,
                                      StreamObserver<ChatgroupResponse> responseObserver) {
        Chatgroup chatgroup = this.chatgroupService.retrieveById(request.getId(), false);

        ChatgroupResponse response;
        if (Objects.isNull(chatgroup)) {
            response = ChatgroupResponse.newBuilder()
                    .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_CHATGROUP_NOT_FOUND_ERROR))
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            return;
        }

        ChatgroupMember chatgroupMember = this.chatgroupService.retrieveChatgroupMemberByGroupIdAndUserId(chatgroup.getId(), request.getUserId());

        response = ChatgroupResponse.newBuilder()
                .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC))
                .setChatgroup(this.chatgroupMessageFactory.valueOf(chatgroup, chatgroupMember))
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * Implements the get chatgroups by island.
     *
     * @param request          {@link RetrieveChatgroupsByIslandIdRequest}.
     * @param responseObserver {@link StreamObserver}.
     */
    @Override
    public void retrieveChatgroupsByIslandId(RetrieveChatgroupsByIslandIdRequest request,
                                             StreamObserver<IslandChatgroupsResponse> responseObserver) {
        PageRequest pageRequest =
                request.hasPageRequest() ? request.getPageRequest() : PaginationUtils.defaultPageRequest();

        Page<Chatgroup> chatgroups = this.chatgroupService.retrieveChatgroupsByIslandId(request.getIslandId(), pageRequest);

        Map<String, ChatgroupMember> chatgroupMembers =
                this.chatgroupService.retrieveChatgroupMemberByGroupIdsAndUserId(chatgroups.get().map(Chatgroup::getId).collect(Collectors.toList()), request.getUserId())
                        .stream().collect(Collectors.toMap(ChatgroupMember::getGroupId, Function.identity(), (mem1, mem2) -> mem1, HashMap::new));

        IslandChatgroupsResponse response = IslandChatgroupsResponse.newBuilder()
                .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC))
                .addAllChatgroups(chatgroups.get()
                        .map(chatgroup -> this.chatgroupMessageFactory.valueOf(chatgroup, chatgroupMembers.getOrDefault(chatgroup.getId(), null)))
                        .collect(Collectors.toList()))
                .setPageResponse(PaginationUtils.valueOf(chatgroups, pageRequest))
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * Implements the retrieve chatgroups by user.
     *
     * @param request          {@link RetreiveChatgroupsByUserIdRequest}.
     * @param responseObserver {@link StreamObserver}.
     */
    @Override
    public void retrieveChatgroupsByUserId(RetreiveChatgroupsByUserIdRequest request,
                                           StreamObserver<UserChatgroupsResponse> responseObserver) {
        List<ChatgroupMember> chatgroupMembers = this.chatgroupService.retrieveChatgroupMembersByUserId(request.getUserId());

        Map<String, ChatgroupMember> chatgroupMemberMap = chatgroupMembers.stream()
                .collect(Collectors.toMap(ChatgroupMember::getGroupId, Function.identity()));

        List<Chatgroup> chatgroups = this.chatgroupService.retrieveChatgroupsByIds(chatgroupMemberMap.keySet());

        UserChatgroupsResponse response = UserChatgroupsResponse.newBuilder()
                .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC))
                .addAllChatgroups(chatgroups.stream()
                        .map(chatgroup -> this.chatgroupMessageFactory.valueOf(chatgroup, chatgroupMemberMap.getOrDefault(chatgroup.getId(), null)))
                        .collect(Collectors.toList()))
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * Implements the retrieve chatgroup members.
     *
     * @param request           {@link RetrieveChatgroupMembersByGroupIdRequest}.
     * @param responseObserver  {@link StreamObserver}.
     */
    @Override
    public void retrieveChatgroupMembersById(RetrieveChatgroupMembersByGroupIdRequest request,
                                             StreamObserver<ChatgroupMembersResponse> responseObserver) {
        ChatgroupMember chatgroupMember = this.chatgroupService.retrieveChatgroupMemberByGroupIdAndUserId(
                request.getGroupId(), request.getUserId());

        if (Objects.isNull(chatgroupMember)) {
            ChatgroupMembersResponse response = ChatgroupMembersResponse.newBuilder()
                    .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_FORBIDDEN))
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            return;
        }

        Page<String> chatgroupMemberIdsPage = this.chatgroupService.retrieveChatgroupMemberUserIdsByGroupId(
                request.getGroupId(), PaginationUtils.valueOf(request.getPageRequest(), "created_time"));

        ChatgroupMembersResponse response = ChatgroupMembersResponse.newBuilder()
                .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC))
                .addAllMemberIds(chatgroupMemberIdsPage.getContent())
                .setPageResponse(PaginationUtils.valueOf(chatgroupMemberIdsPage, request.getPageRequest()))
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

}
