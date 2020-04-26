package com.keepreal.madagascar.lemur.dtoFactory;

import com.keepreal.madagascar.common.Gender;
import com.keepreal.madagascar.common.UserMessage;
import org.springframework.stereotype.Component;
import swagger.model.IdentityType;
import swagger.model.UserDTO;

import java.sql.Date;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents the user dto factory.
 */
@Component
public class UserDTOFactory {

    /**
     * Converts {@link UserMessage} to {@link UserDTO}.
     *
     * @param user {@link UserMessage}.
     * @return {@link UserDTO}.
     */
    public UserDTO valueOf(UserMessage user) {
        if (Objects.isNull(user)) {
            return null;
        }

        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setName(user.getName());
        userDTO.setCity(user.getCity());
        userDTO.setBirthday(Date.valueOf(user.getBirthday()));
        userDTO.setDescription(user.getDescription());
        userDTO.setPortraitImageUri(user.getPortraitImageUri());
        userDTO.setGender(this.convertGender(user.getGender()));
        userDTO.setIdentityTypes(user.getIdentitiesList()
                .stream()
                .map(this::convertIdentityType)
                .collect(Collectors.toList()));

        return userDTO;
    }

    /**
     * Converts {@link Gender} to {@link UserDTO.GenderEnum}.
     *
     * @param gender {@link Gender}.
     * @return {@link UserDTO.GenderEnum}.
     */
    private UserDTO.GenderEnum convertGender(Gender gender) {
        switch (gender) {
            case MALE:
                return UserDTO.GenderEnum.NUMBER_0;
            case FEMALE:
                return UserDTO.GenderEnum.NUMBER_1;
            default:
                return UserDTO.GenderEnum.NUMBER_0;
        }
    }

    /**
     * Converts {@link com.keepreal.madagascar.common.IdentityType} to {@link IdentityType}.
     *
     * @param identityType {@link com.keepreal.madagascar.common.IdentityType}.
     * @return {@link IdentityType}.
     */
    private IdentityType convertIdentityType(com.keepreal.madagascar.common.IdentityType identityType) {
        switch (identityType) {
            case IDENTITY_COMIC:
                return IdentityType.COMIC;
            case IDENTITY_DANCING:
                return IdentityType.DANCING;
            case IDENTITY_FASHION:
                return IdentityType.FASHION;
            case IDENTITY_FOOD:
                return IdentityType.FOOD;
            case IDENTITY_GAMING:
                return IdentityType.GAMING;
            case IDENTITY_GEEK:
                return IdentityType.GEEK;
            case IDENTITY_MUSIC:
                return IdentityType.MUSIC;
            case IDENTITY_PAINTING:
                return IdentityType.PAINTING;
            case IDENTITY_PHOTOGRAPH:
                return IdentityType.PHOTOGRAPH;
            case IDENTITY_TRAVEL:
                return IdentityType.TRAVEL;
            case IDENTITY_VIDEO:
                return IdentityType.VIDEO;
            case IDENTITY_VLOG:
                return IdentityType.VLOG;
            case IDENTITY_WRITING:
                return IdentityType.WRITING;
            case IDENTITY_OTHERS:
            default:
                return IdentityType.OTHERS;
        }
    }

}