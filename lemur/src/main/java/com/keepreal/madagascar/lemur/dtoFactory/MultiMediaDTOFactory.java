package com.keepreal.madagascar.lemur.dtoFactory;

import com.keepreal.madagascar.common.AnswerMessage;
import com.keepreal.madagascar.common.AudioMessage;
import com.keepreal.madagascar.common.FeedMessage;
import com.keepreal.madagascar.common.HtmlMessage;
import com.keepreal.madagascar.common.Picture;
import com.keepreal.madagascar.common.VideoMessage;
import com.keepreal.madagascar.lemur.service.UserService;
import swagger.model.MultiMediaDTO;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class MultiMediaDTOFactory {

    private final UserService userService;
    private final UserDTOFactory userDTOFactory;

    public MultiMediaDTOFactory(UserService userService,
                                UserDTOFactory userDTOFactory) {
        this.userService = userService;
        this.userDTOFactory = userDTOFactory;
    }

    public List<MultiMediaDTO> listValueOf(FeedMessage feedMessage) {
        MultiMediaDTO dto = new MultiMediaDTO();

        switch (feedMessage.getType()) {
            case MEDIA_TEXT:
                return Collections.emptyList();
            case MEDIA_PICS:
            case MEDIA_ALBUM:
                return feedMessage.getPics().getPictureList()
                        .stream()
                        .map(this::valueOf)
                        .collect(Collectors.toList());
            case MEDIA_VIDEO:
                dto = this.valueOf(feedMessage.getVideo());
                break;
            case MEDIA_AUDIO:
                dto = this.valueOf(feedMessage.getAudio());
                break;
            case MEDIA_HTML:
                dto = this.valueOf(feedMessage.getHtml());
                break;
            case MEDIA_QUESTION:
                dto = this.valueOf(feedMessage.getAnswer(), feedMessage.getCreatedAt());
        }
        return Collections.singletonList(dto);
    }

    private MultiMediaDTO valueOf(AnswerMessage answerMessage, long creatTimestamp) {
        MultiMediaDTO dto = new MultiMediaDTO();
        dto.setHasExpired(creatTimestamp > System.currentTimeMillis());
        if (Objects.isNull(answerMessage)) {
            return dto;
        }

        if (answerMessage.hasAnswer()) {
            dto.setText(answerMessage.getAnswer().getValue());
        }
        if (answerMessage.hasAnsweredAt()) {
            dto.setAnsweredAt(answerMessage.getAnsweredAt().getValue());
        }
        if (answerMessage.hasPublicVisible()) {
            dto.setPublicVisible(answerMessage.getPublicVisible().getValue());
        }
        if (answerMessage.hasAnswerUserId()) {
            dto.setUser(this.userDTOFactory.briefValueOf(this.userService.retrieveUserById(answerMessage.getAnswerUserId().getValue())));
        }

        return dto;
    }

    private MultiMediaDTO valueOf(Picture picture) {
        MultiMediaDTO dto = new MultiMediaDTO();
        dto.setUrl(picture.getImgUrl());
        dto.setWidth((int) picture.getWidth());
        dto.setHeight((int) picture.getHeight());
        dto.setSize(picture.getSize());

        return dto;
    }

    private MultiMediaDTO valueOf(VideoMessage videoMessage) {
        MultiMediaDTO dto = new MultiMediaDTO();
        dto.setUrl(videoMessage.getUrl());
        dto.setTitle(videoMessage.getTitle());
        dto.setThumbnailUrl(videoMessage.getThumbnailUrl());
        dto.setWidth((int) videoMessage.getWidth());
        dto.setHeight((int) videoMessage.getHeight());
        dto.setLength((int) videoMessage.getDuration());
        dto.setVideoId(videoMessage.getVideoId());

        return dto;
    }

    private MultiMediaDTO valueOf(AudioMessage audioMessage) {
        MultiMediaDTO dto = new MultiMediaDTO();
        dto.setUrl(audioMessage.getUrl());
        dto.setTitle(audioMessage.getTitle());
        dto.setThumbnailUrl(audioMessage.getThumbnailUrl());
        dto.setLength((int) audioMessage.getDuration());
        dto.setVideoId(audioMessage.getVideoId());

        return dto;
    }

    private MultiMediaDTO valueOf(HtmlMessage htmlMessage) {
        MultiMediaDTO dto = new MultiMediaDTO();
        dto.setContent(htmlMessage.getContent());

        return dto;
    }
}
