package com.keepreal.madagascar.lemur.dtoFactory;

import com.keepreal.madagascar.common.Gender;
import com.keepreal.madagascar.common.UserMessage;
import org.springframework.stereotype.Component;
import swagger.model.BriefUserDTO;
import swagger.model.GenderType;
import swagger.model.IdentityType;
import swagger.model.UserDTO;

import java.sql.Date;
import java.time.LocalDate;
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
        userDTO.setDisplayId(user.getDisplayId());
        userDTO.setName(user.getName());
        userDTO.setCity(user.getCity());
        userDTO.setBirthday(Date.valueOf(user.getBirthday()));
        userDTO.setDescription(user.getDescription());
        userDTO.setPortraitImageUri(user.getPortraitImageUri());
        userDTO.setGender(this.convertGender(user.getGender()));
        userDTO.setAge(LocalDate.now().getYear() - Date.valueOf(user.getBirthday()).toLocalDate().getYear());
        userDTO.setCreatedAt(user.getCreatedAt());
        userDTO.setIdentityTypes(user.getIdentitiesList()
                .stream()
                .map(this::convertIdentityType)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));

        return userDTO;
    }

    /**
     * Converts {@link UserMessage} to {@link BriefUserDTO}.
     *
     * @param user {@link UserMessage}.
     * @return {@link BriefUserDTO}.
     */
    public BriefUserDTO briefValueOf(UserMessage user) {
        if (Objects.isNull(user)) {
            return null;
        }

        BriefUserDTO briefUserDTO = new BriefUserDTO();
        briefUserDTO.setId(user.getId());
        briefUserDTO.setDisplayId(user.getDisplayId());
        briefUserDTO.setName(user.getName());
        briefUserDTO.setPortraitImageUri(user.getPortraitImageUri());
        briefUserDTO.setGender(this.convertGender(user.getGender()));

        return briefUserDTO;
    }

    /**
     * Converts {@link Gender} to {@link GenderType}.
     *
     * @param gender {@link Gender}.
     * @return {@link GenderType}.
     */
    private GenderType convertGender(Gender gender) {
        if (Objects.isNull(gender)) {
            return null;
        }

        switch (gender) {
            case UNKNOWN:
                return GenderType.NUMBER_0;
            case MALE:
                return GenderType.NUMBER_1;
            case FEMALE:
                return GenderType.NUMBER_2;
            default:
                return null;
        }
    }

    /**
     * Converts {@link com.keepreal.madagascar.common.IdentityType} to {@link IdentityType}.
     *
     * @param identityType {@link com.keepreal.madagascar.common.IdentityType}.
     * @return {@link IdentityType}.
     */
    private IdentityType convertIdentityType(com.keepreal.madagascar.common.IdentityType identityType) {
        if (Objects.isNull(identityType)) {
            return null;
        }

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