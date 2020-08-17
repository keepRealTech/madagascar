package com.keepreal.madagascar.lemur.controller;

import com.keepreal.madagascar.common.Gender;
import com.keepreal.madagascar.common.IdentityType;
import com.keepreal.madagascar.common.IslandMessage;
import com.keepreal.madagascar.common.UserMessage;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.coua.MembershipMessage;
import com.keepreal.madagascar.lemur.dtoFactory.MembershipDTOFactory;
import com.keepreal.madagascar.lemur.dtoFactory.UserDTOFactory;
import com.keepreal.madagascar.lemur.service.ImageService;
import com.keepreal.madagascar.lemur.service.IslandService;
import com.keepreal.madagascar.lemur.service.MembershipService;
import com.keepreal.madagascar.lemur.service.SubscribeMembershipService;
import com.keepreal.madagascar.lemur.service.UserService;
import com.keepreal.madagascar.lemur.textFilter.TextContentFilter;
import com.keepreal.madagascar.lemur.util.HttpContextUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import swagger.api.UserApi;
import swagger.model.AvatarsResponse;
import swagger.model.BriefMembershipDTO;
import swagger.model.ChatUsersResponse;
import swagger.model.FullUserResponse;
import swagger.model.GenderType;
import swagger.model.PostBatchGetUsersRequest;
import swagger.model.PutUserMobileRequest;
import swagger.model.PutUserPayload;
import swagger.model.UserResponse;

import javax.validation.Valid;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Represents the user controller.
 */
@RestController
@Slf4j
public class UserController implements UserApi {

    private final ImageService imageService;
    private final UserService userService;
    private final IslandService islandService;
    private final MembershipService membershipService;
    private final SubscribeMembershipService subscribeMembershipService;
    private final UserDTOFactory userDTOFactory;
    private final MembershipDTOFactory membershipDTOFactory;
    private final TextContentFilter textContentFilter;

    /**
     * Constructs the user controller.
     *
     * @param imageService               {@link ImageService}.
     * @param userService                {@link UserService}.
     * @param islandService              {@link IslandService}.
     * @param membershipService          {@link MembershipService}.
     * @param subscribeMembershipService {@link SubscribeMembershipService}.
     * @param userDTOFactory             {@link UserDTOFactory}.
     * @param membershipDTOFactory       {@link MembershipDTOFactory}.
     * @param textContentFilter          {@link TextContentFilter}.
     */
    public UserController(ImageService imageService,
                          UserService userService,
                          IslandService islandService,
                          MembershipService membershipService,
                          SubscribeMembershipService subscribeMembershipService,
                          UserDTOFactory userDTOFactory,
                          MembershipDTOFactory membershipDTOFactory,
                          TextContentFilter textContentFilter) {
        this.imageService = imageService;
        this.userService = userService;
        this.islandService = islandService;
        this.membershipService = membershipService;
        this.subscribeMembershipService = subscribeMembershipService;
        this.userDTOFactory = userDTOFactory;
        this.membershipDTOFactory = membershipDTOFactory;
        this.textContentFilter = textContentFilter;
    }

    /**
     * Implements the get user by id api.
     *
     * @param id User id.
     * @return {@link FullUserResponse}.
     */
    @Override
    public ResponseEntity<FullUserResponse> apiV1UsersIdGet(String id) {
        UserMessage userMessage = this.userService.retrieveUserById(id);

        FullUserResponse response = new FullUserResponse();
        response.setData(this.userDTOFactory.fullValueOf(userMessage));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the update user by id api.
     *
     * @param id            User id.
     * @param payload       {@link PutUserPayload}.
     * @param portraitImage The user portrait image to update.
     * @return {@link UserResponse}.
     */
    @Override
    public ResponseEntity<UserResponse> apiV1UsersIdPut(String id,
                                                        PutUserPayload payload,
                                                        @RequestPart(value = "portraitImage", required = false) MultipartFile portraitImage) {
        String userId = HttpContextUtils.getUserIdFromContext();
        if (Objects.isNull(payload)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (this.textContentFilter.isDisallowed(payload.getName())) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_NAME_INVALID);
        }

        if (!userId.equals(id)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        String portraitImageUri = null;
        if (Objects.nonNull(portraitImage)) {
            portraitImageUri = this.imageService.uploadSingleImage(portraitImage);
        }

        List<swagger.model.IdentityType> identityTypeList =
                Objects.nonNull(payload.getIdentityTypes()) ? payload.getIdentityTypes() : new ArrayList<>();

        String birthday = null;
        if (Objects.nonNull(payload.getBirthday())) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            birthday = formatter.format(payload.getBirthday());
        }

        UserMessage userMessage = this.userService.updateUser(id,
                payload.getName(),
                portraitImageUri,
                this.convertGenderEnum(payload.getGender()),
                payload.getDescription(),
                payload.getCity(),
                birthday,
                identityTypeList.stream().map(this::convertIdentityType).collect(Collectors.toList()));

        UserResponse response = new UserResponse();
        response.setData(this.userDTOFactory.valueOf(userMessage));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the get batch user avatars api.
     *
     * @param postBatchGetUsersRequest (required) {@link PostBatchGetUsersRequest}.
     * @return {@link AvatarsResponse}.
     */
    @Override
    public ResponseEntity<AvatarsResponse> apiV1UsersGetBatchAvatarsPost(PostBatchGetUsersRequest postBatchGetUsersRequest) {
        List<UserMessage> userMessages;
        if (Objects.isNull(postBatchGetUsersRequest.getUserIds()) || postBatchGetUsersRequest.getUserIds().isEmpty()) {
            userMessages = new ArrayList<>();
        } else {
            userMessages = this.userService.retrieveUsersByIds(postBatchGetUsersRequest.getUserIds());
        }

        AvatarsResponse response = new AvatarsResponse();
        response.setData(userMessages.stream()
                .map(this.userDTOFactory::avatorValueOf)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the get user info in batch api for chat list.
     *
     * @param postBatchGetUsersRequest (required) {@link PostBatchGetUsersRequest}.
     * @return {@link ChatUsersResponse}.
     */
    @Override
    public ResponseEntity<ChatUsersResponse> apiV1UsersGetChatUserInfosPost(PostBatchGetUsersRequest postBatchGetUsersRequest) {
        String userId = HttpContextUtils.getUserIdFromContext();

        List<UserMessage> userMessages;
        if (Objects.isNull(postBatchGetUsersRequest.getUserIds()) || postBatchGetUsersRequest.getUserIds().isEmpty()) {
            ChatUsersResponse response = new ChatUsersResponse();
            response.setData(new ArrayList<>());
            response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
            response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            userMessages = this.userService.retrieveUsersByIds(postBatchGetUsersRequest.getUserIds());
        }

        if (userMessages.isEmpty()) {
            ChatUsersResponse response = new ChatUsersResponse();
            response.setData(new ArrayList<>());
            response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
            response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        List<IslandMessage> createdIslandList = this.islandService.retrieveIslands(null, userId, null, 0, 1).getIslandsList();

        Map<String, List<BriefMembershipDTO>> membershipMap = new HashMap<>();
        if (!createdIslandList.isEmpty()) {
            String islandId = createdIslandList.get(0).getId();
            Map<String, MembershipMessage> islandMembershipMap = this.membershipService.retrieveMembershipsByIslandId(islandId).stream()
                    .collect(Collectors.toMap(MembershipMessage::getId, Function.identity(), (mem1, mem2) -> mem1, HashMap::new));
            userMessages.forEach(user -> {
                List<String> membershipIds = this.subscribeMembershipService.retrieveSubscribedMembershipsByIslandIdAndUserId(islandId, user.getId());
                List<BriefMembershipDTO> briefMembershipDTOList = membershipIds.stream()
                        .map(membershipId -> islandMembershipMap.getOrDefault(membershipId, null))
                        .map(this.membershipDTOFactory::briefValueOf)
                        .collect(Collectors.toList());
                membershipMap.put(user.getId(), briefMembershipDTOList);
            });
        }

        ChatUsersResponse response = new ChatUsersResponse();
        response.setData(userMessages.stream()
                .map(user -> this.userDTOFactory.chatUserValueOf(user, membershipMap.getOrDefault(user.getId(), null)))
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 更新当前用户手机号
     *
     * @param putUserMobileRequest  (required) {@link PutUserMobileRequest}
     * @return {@link UserResponse}
     */
    @Override
    public ResponseEntity<UserResponse> apiV1UsersMobilePut(@Valid PutUserMobileRequest putUserMobileRequest) {
        UserResponse response = new UserResponse();

        if (Objects.isNull(putUserMobileRequest.getMobile()) || Objects.isNull(putUserMobileRequest.getOtp())) {
            response.setRtn(ErrorCode.REQUEST_INVALID_ARGUMENT.getNumber());
            response.setMsg(ErrorCode.REQUEST_INVALID_ARGUMENT.getValueDescriptor().getName());
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
        }

        com.keepreal.madagascar.coua.UserResponse userResponse = this.userService.updateUserMobilePhone(putUserMobileRequest);
        if (ErrorCode.REQUEST_SUCC_VALUE == userResponse.getStatus().getRtn()) {
            response.setData(this.userDTOFactory.valueOf(userResponse.getUser()));
            response.setRtn(userResponse.getStatus().getRtn());
            response.setMsg(userResponse.getStatus().getMessage());
        } else {
            response.setRtn(userResponse.getStatus().getRtn());
            response.setMsg(userResponse.getStatus().getMessage());
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Converts {@link GenderType} to {@link Gender}.
     *
     * @param genderType {@link GenderType}.
     * @return {@link Gender}.
     */
    private Gender convertGenderEnum(GenderType genderType) {
        if (Objects.isNull(genderType)) {
            return null;
        }

        switch (genderType) {
            case NUMBER_0:
                return Gender.UNKNOWN;
            case NUMBER_1:
                return Gender.MALE;
            case NUMBER_2:
                return Gender.FEMALE;
            default:
                return Gender.UNRECOGNIZED;
        }
    }

    /**
     * Converts the {@link swagger.model.IdentityType} into {@link IdentityType}.
     *
     * @param identityType {@link swagger.model.IdentityType}.
     * @return {@link IdentityType}.
     */
    private IdentityType convertIdentityType(swagger.model.IdentityType identityType) {
        switch (identityType) {
            case COMIC:
                return IdentityType.IDENTITY_COMIC;
            case DANCING:
                return IdentityType.IDENTITY_DANCING;
            case FASHION:
                return IdentityType.IDENTITY_FASHION;
            case FOOD:
                return IdentityType.IDENTITY_FOOD;
            case GAMING:
                return IdentityType.IDENTITY_GAMING;
            case GEEK:
                return IdentityType.IDENTITY_GEEK;
            case MUSIC:
                return IdentityType.IDENTITY_MUSIC;
            case PAINTING:
                return IdentityType.IDENTITY_PAINTING;
            case PHOTOGRAPH:
                return IdentityType.IDENTITY_PHOTOGRAPH;
            case TRAVEL:
                return IdentityType.IDENTITY_TRAVEL;
            case VIDEO:
                return IdentityType.IDENTITY_VIDEO;
            case VLOG:
                return IdentityType.IDENTITY_VLOG;
            case WRITING:
                return IdentityType.IDENTITY_WRITING;
            case OTHERS:
                return IdentityType.IDENTITY_OTHERS;
            default:
                return IdentityType.UNRECOGNIZED;
        }
    }

}
