package com.keepreal.madagascar.asity.grpcController;

import com.keepreal.madagascar.asity.ChatServiceGrpc;
import com.keepreal.madagascar.asity.ChatgroupResponse;
import com.keepreal.madagascar.asity.CreateChatgroupRequest;
import com.keepreal.madagascar.asity.DismissChatgroupRequest;
import com.keepreal.madagascar.asity.EnableChatAccessRequest;
import com.keepreal.madagascar.asity.IslandChatAccessResponse;
import com.keepreal.madagascar.asity.RegisterRequest;
import com.keepreal.madagascar.asity.RegisterResponse;
import com.keepreal.madagascar.asity.RetrieveChatAccessByIslandIdAndUserIdRequest;
import com.keepreal.madagascar.asity.UpdateChatgroupRequest;
import com.keepreal.madagascar.asity.factory.ChatgroupMessageFactory;
import com.keepreal.madagascar.asity.model.Chatgroup;
import com.keepreal.madagascar.asity.model.ChatgroupMember;
import com.keepreal.madagascar.asity.model.IslandChatAccess;
import com.keepreal.madagascar.asity.service.ChatgroupService;
import com.keepreal.madagascar.asity.service.IslandChatAccessService;
import com.keepreal.madagascar.asity.service.RongCloudService;
import com.keepreal.madagascar.asity.util.CommonStatusUtils;
import com.keepreal.madagascar.common.CommonStatus;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;

import javax.transaction.Transactional;
import java.util.Objects;

/**
 * Represents the chat grpc service.
 */
@GRpcService
public class ChatController extends ChatServiceGrpc.ChatServiceImplBase {

    private final ChatgroupService chatgroupService;
    private final RongCloudService rongCloudService;
    private final IslandChatAccessService islandChatAccessService;

    private final ChatgroupMessageFactory chatgroupMessageFactory;

    /**
     * Constructs the chat controller.
     *
     * @param chatgroupService
     * @param rongCloudService        {@link RongCloudService}.
     * @param islandChatAccessService {@link IslandChatAccessService}.
     * @param chatgroupMessageFactory
     */
    public ChatController(ChatgroupService chatgroupService,
                          RongCloudService rongCloudService,
                          IslandChatAccessService islandChatAccessService,
                          ChatgroupMessageFactory chatgroupMessageFactory) {
        this.chatgroupService = chatgroupService;
        this.rongCloudService = rongCloudService;
        this.islandChatAccessService = islandChatAccessService;
        this.chatgroupMessageFactory = chatgroupMessageFactory;
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
        this.islandChatAccessService.enable(request.getIslandId(), request.getUserId());

        CommonStatus response = CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * Retrieves the chat access for an island and user.
     *
     * @param request          {@link RetrieveChatAccessByIslandIdAndUserIdRequest}.
     * @param responseObserver {@link StreamObserver}.
     */
    @Override
    public void retrieveChatAccessByIslandIdAndUserId(RetrieveChatAccessByIslandIdAndUserIdRequest request,
                                                      StreamObserver<IslandChatAccessResponse> responseObserver) {
        IslandChatAccess islandChatAccess = this.islandChatAccessService.createIslandChatAccess(request.getIslandId(), request.getUserId());

        // TODO: FILL THE RESPONSE
        IslandChatAccessResponse response = IslandChatAccessResponse.newBuilder()
                .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC))
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
        Chatgroup chatgroup= this.chatgroupService.retrieveById(request.getId(), false);

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

        if ((request.hasName() || request.hasBulletin() || Objects.nonNull(request.getMembershipIdsList()))
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
            chatgroup.setBulletin(request.getBulletin().getValue());
        }

        if (Objects.nonNull(request.getMembershipIdsList())) {
            chatgroup = this.chatgroupService.updateChatgroupMembershipInMem(chatgroup, request.getMembershipIdsList());
        }

        chatgroup = this.chatgroupService.upsert(chatgroup);

        response = ChatgroupResponse.newBuilder()
                .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_CHATGROUP_NOT_FOUND_ERROR))
                .setChatgroup(this.chatgroupMessageFactory.valueOf(chatgroup, chatgroupMember))
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

}
