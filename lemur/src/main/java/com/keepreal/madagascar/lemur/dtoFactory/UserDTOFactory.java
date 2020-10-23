package com.keepreal.madagascar.lemur.dtoFactory;

import com.keepreal.madagascar.common.Gender;
import com.keepreal.madagascar.common.UserMessage;
import com.keepreal.madagascar.coua.QualificationMessage;
import com.keepreal.madagascar.lemur.service.IslandService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import swagger.model.AvatarDTO;
import swagger.model.BriefIslandDTO;
import swagger.model.BriefMembershipDTO;
import swagger.model.BriefUserDTO;
import swagger.model.ChatUserDTO;
import swagger.model.FullUserDTO;
import swagger.model.GenderType;
import swagger.model.IdentityType;
import swagger.model.QualificationChannelDTO;
import swagger.model.UserDTO;
import swagger.model.UserQualificationDTO;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents the user dto factory.
 */
@Component
public class UserDTOFactory {

    private final List<String> channelList = Arrays.asList("微博", "抖音", "哔哩哔哩", "快手", "Lofter", "快看漫画", "晋江", "阅文", "起点读书", "网易云音乐", "喜马拉雅", "小宇宙", "荔枝", "其他");

    /**
     * Converts {@link UserMessage} to {@link UserDTO}.
     *
     * @param user              {@link UserMessage}.
     * @return {@link UserDTO}.
     */
    public UserDTO valueOf(UserMessage user) {
        return this.valueOf(user, true);
    }

    /**
     * Converts {@link UserMessage} to {@link UserDTO}.
     *
     * @param user              {@link UserMessage}.
     * @param shouldMaskMobile  Whether should mask user mobile.
     * @return {@link UserDTO}.
     */
    public UserDTO valueOf(UserMessage user, Boolean shouldMaskMobile) {
        if (Objects.isNull(user)) {
            return null;
        }

        String mobile = user.getMobile();
        String code = "86";
        String[] split = StringUtils.split(user.getMobile(), "-");
        if (Objects.nonNull(split)) {
            code = split[0];
            mobile = split[1];
        }

        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setDisplayId(user.getDisplayId());
        userDTO.setName(user.getName());
        userDTO.setCity(user.getCity());
        userDTO.setBirthday(Date.valueOf(user.getBirthday()));
        userDTO.setDescription(user.getDescription());
        userDTO.setPortraitImageUri(this.generatePortraitImageUri(user));
        userDTO.setGender(this.convertGender(user.getGender()));
        userDTO.setAge(LocalDate.now().getYear() - Date.valueOf(user.getBirthday()).toLocalDate().getYear());
        userDTO.setCreatedAt(user.getCreatedAt());
        userDTO.setCode(code);
        userDTO.setMobile(shouldMaskMobile ? "***********" : mobile);
        userDTO.setHasPassword(!StringUtils.isEmpty(user.getPassword()));

        userDTO.setIdentityTypes(user.getIdentitiesList()
                .stream()
                .map(this::convertIdentityType)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));

        return userDTO;
    }

    /**
     * Converts {@link UserMessage} to {@link FullUserDTO}.
     *
     * @param user             {@link UserMessage}.
     * @param shouldMaskMobile Whether should mask user mobile.
     * @return {@link FullUserDTO}.
     */
    public FullUserDTO fullValueOf(UserMessage user, Boolean shouldMaskMobile, List<BriefIslandDTO> createdIslands) {
        if (Objects.isNull(user)) {
            return null;
        }

        String mobile = user.getMobile();
        String code = "86";
        String[] split = StringUtils.split(user.getMobile(), "-");
        if (Objects.nonNull(split)) {
            code = split[0];
            mobile = split[1];
        }

        FullUserDTO fullUserDTO = new FullUserDTO();
        fullUserDTO.setId(user.getId());
        fullUserDTO.setDisplayId(user.getDisplayId());
        fullUserDTO.setName(user.getName());
        fullUserDTO.setCity(user.getCity());
        fullUserDTO.setBirthday(Date.valueOf(user.getBirthday()));
        fullUserDTO.setDescription(user.getDescription());
        fullUserDTO.setPortraitImageUri(this.generatePortraitImageUri(user));
        fullUserDTO.setGender(this.convertGender(user.getGender()));
        fullUserDTO.setAge(LocalDate.now().getYear() - Date.valueOf(user.getBirthday()).toLocalDate().getYear());
        fullUserDTO.setCreatedAt(user.getCreatedAt());
        fullUserDTO.setMobile(shouldMaskMobile ? "***********" : user.getMobile());
        fullUserDTO.setCode(code);
        fullUserDTO.setMobile(shouldMaskMobile ? "***********" : mobile);
        fullUserDTO.setHasPassword(!StringUtils.isEmpty(user.getPassword()));

        fullUserDTO.setIdentityTypes(
                user.getIdentitiesList()
                        .stream()
                        .map(this::convertIdentityType)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()));

        fullUserDTO.setCreatedIslands(createdIslands);

        return fullUserDTO;
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
        briefUserDTO.setPortraitImageUri(this.generatePortraitImageUri(user));
        briefUserDTO.setGender(this.convertGender(user.getGender()));
        briefUserDTO.setAge(LocalDate.now().getYear() - Date.valueOf(user.getBirthday()).toLocalDate().getYear());
        briefUserDTO.setIdentityTypes(user.getIdentitiesList()
                .stream()
                .map(this::convertIdentityType)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));

        return briefUserDTO;
    }

    /**
     * Converts {@link UserMessage} into {@link AvatarDTO}.
     *
     * @param user {@link UserMessage}.
     * @return {@link AvatarDTO}.
     */
    public AvatarDTO avatorValueOf(UserMessage user) {
        if (Objects.isNull(user)) {
            return null;
        }

        AvatarDTO avatarDTO = new AvatarDTO();
        avatarDTO.setUserId(user.getId());
        avatarDTO.setPortraitUrl(this.generatePortraitImageUri(user));

        return avatarDTO;
    }

    /**
     * Builds the {@link ChatUserDTO} from {@link UserMessage} and {@link BriefMembershipDTO}.
     *
     * @param user                   {@link UserMessage}.
     * @param briefMembershipDTOList {@link BriefMembershipDTO}.
     * @return {@link ChatUserDTO}.
     */
    public ChatUserDTO chatUserValueOf(UserMessage user, List<BriefMembershipDTO> briefMembershipDTOList) {
        if (Objects.isNull(user)) {
            return null;
        }

        if (Objects.isNull(briefMembershipDTOList)) {
            briefMembershipDTOList = new ArrayList<>();
        }

        ChatUserDTO chatUserDTO = new ChatUserDTO();
        chatUserDTO.setId(user.getId());
        chatUserDTO.setDisplayId(user.getDisplayId());
        chatUserDTO.setName(user.getName());
        chatUserDTO.setPortraitImageUri(this.generatePortraitImageUri(user));
        chatUserDTO.setGender(this.convertGender(user.getGender()));
        chatUserDTO.setAge(LocalDate.now().getYear() - Date.valueOf(user.getBirthday()).toLocalDate().getYear());

        chatUserDTO.setMemberships(briefMembershipDTOList.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(BriefMembershipDTO::getChargePerMonth, Comparator.reverseOrder()))
                .collect(Collectors.toList()));

        return chatUserDTO;
    }

    public List<QualificationChannelDTO> listValueOf() {
        return channelList.stream().map(channel -> {
            QualificationChannelDTO dto = new QualificationChannelDTO();
            dto.setName(channel);
            return dto;
        }).collect(Collectors.toList());
    }

    public UserQualificationDTO valueOf(QualificationMessage message) {
        UserQualificationDTO dto = new UserQualificationDTO();
        dto.setId(message.getId());
        dto.setName(message.getName());
        dto.setHostUrl(message.getUrl());
        return dto;
    }

    /**
     * Converts {@link Gender} to {@link GenderType}.
     *
     * @param gender {@link Gender}.
     * @return {@link GenderType}.
     */
    private GenderType convertGender(Gender gender) {
        if (Objects.isNull(gender)) {
            return GenderType.NUMBER_0;
        }

        switch (gender) {
            case UNKNOWN:
                return GenderType.NUMBER_0;
            case MALE:
                return GenderType.NUMBER_1;
            case FEMALE:
                return GenderType.NUMBER_2;
            case UNSET:
            default:
                return GenderType.NUMBER_3;
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

    /**
     * Configures default user portrait if none set.
     *
     * @param user {@link UserMessage}.
     * @return Image url.
     */
    private String generatePortraitImageUri(UserMessage user) {
        if (!StringUtils.isEmpty(user.getPortraitImageUri())) {
            return user.getPortraitImageUri();
        }

        int hash = Math.abs(user.getId().hashCode() % 5) + 1;
        switch (Objects.requireNonNull(this.convertGender(user.getGender()))) {
            case NUMBER_1:
                return String.format("md-%d.png", hash);
            case NUMBER_2:
                return String.format("fd-%d.png", hash);
            case NUMBER_0:
            case NUMBER_3:
            default:
                return String.format("ud-%d.png", hash);
        }
    }

}