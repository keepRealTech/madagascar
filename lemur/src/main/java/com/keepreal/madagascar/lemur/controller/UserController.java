package com.keepreal.madagascar.lemur.controller;

import com.keepreal.madagascar.common.Gender;
import com.keepreal.madagascar.common.IdentityType;
import com.keepreal.madagascar.common.UserMessage;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.lemur.dtoFactory.UserDTOFactory;
import com.keepreal.madagascar.lemur.service.ImageService;
import com.keepreal.madagascar.lemur.service.UserService;
import com.keepreal.madagascar.lemur.util.ResponseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import swagger.api.UserApi;
import swagger.model.GenderType;
import swagger.model.PutUserPayload;
import swagger.model.UserResponse;

import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents the user controller.
 */
@RestController
@Slf4j
public class UserController implements UserApi {

    private final ImageService imageService;
    private final UserService userService;
    private final UserDTOFactory userDTOFactory;

    /**
     * Constructs the user controller.
     *
     * @param imageService   {@link ImageService}.
     * @param userService    {@link UserService}.
     * @param userDTOFactory {@link UserDTOFactory}.
     */
    public UserController(ImageService imageService,
                          UserService userService,
                          UserDTOFactory userDTOFactory) {
        this.imageService = imageService;
        this.userService = userService;
        this.userDTOFactory = userDTOFactory;
    }

    /**
     * Implements the get user by id api.
     *
     * @param id User id.
     * @return {@link UserResponse}.
     */
    @Override
    public ResponseEntity<UserResponse> apiV1UsersIdGet(String id) {
        UserMessage userMessage = this.userService.retrieveUserById(id);

        UserResponse response = new UserResponse();
        response.setData(this.userDTOFactory.valueOf(userMessage));
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
        String portraitImageUri = null;
        if (Objects.nonNull(portraitImage)) {
            portraitImageUri = this.imageService.uploadSingleImageAsync(portraitImage);
        }

        UserMessage userMessage = this.userService.updateUser(id,
                payload.getName(),
                portraitImageUri,
                this.convertGenderEnum(payload.getGender()),
                payload.getDescription(),
                payload.getCity(),
                payload.getBirthday().toString(),
                payload.getIdentityTypes().stream().map(this::convertIdentityType).collect(Collectors.toList()));

        UserResponse response = new UserResponse();
        response.setData(this.userDTOFactory.valueOf(userMessage));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
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
                return Gender.MALE;
            case NUMBER_1:
                return Gender.FEMALE;
            default:
                return Gender.UNKNOWN;
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
