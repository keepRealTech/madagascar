package com.keepreal.madagascar.fossa.util;

import com.google.protobuf.BoolValue;
import com.google.protobuf.Int64Value;
import com.google.protobuf.StringValue;
import com.keepreal.madagascar.common.AudioMessage;
import com.keepreal.madagascar.common.HtmlMessage;
import com.keepreal.madagascar.common.Picture;
import com.keepreal.madagascar.common.PicturesMessage;
import com.keepreal.madagascar.common.AnswerMessage;
import com.keepreal.madagascar.common.VideoMessage;
import com.keepreal.madagascar.fossa.model.AudioInfo;
import com.keepreal.madagascar.fossa.model.HtmlInfo;
import com.keepreal.madagascar.fossa.model.MediaInfo;
import com.keepreal.madagascar.fossa.model.PictureInfo;
import com.keepreal.madagascar.fossa.model.AnswerInfo;
import com.keepreal.madagascar.fossa.model.VideoInfo;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-08-04
 **/

public class MediaMessageConvertUtils {

    public static List<PictureInfo> toPictureInfoList(PicturesMessage picturesMessage) {
        return picturesMessage.getPictureList()
                .stream()
                .map(picture -> {
                    PictureInfo pictureInfo = new PictureInfo();
                    pictureInfo.setUrl(picture.getImgUrl());
                    pictureInfo.setWidth(picture.getWidth());
                    pictureInfo.setHeight(picture.getHeight());
                    pictureInfo.setSize(picture.getSize());
                    return pictureInfo;
                })
                .collect(Collectors.toList());
    }

    public static VideoInfo toVideoInfo(VideoMessage videoMessage) {
        VideoInfo videoInfo = new VideoInfo();
        videoInfo.setUrl(videoMessage.getUrl());
        videoInfo.setTitle(videoMessage.getTitle());
        videoInfo.setThumbnailUrl(videoMessage.getThumbnailUrl());
        videoInfo.setDuration(videoMessage.getDuration());
        videoInfo.setWidth(videoMessage.getWidth());
        videoInfo.setHeight(videoMessage.getHeight());
        videoInfo.setVideoId(videoMessage.getVideoId());

        return videoInfo;
    }

    public static AudioInfo toAudioInfo(AudioMessage audioMessage) {
        AudioInfo audioInfo = new AudioInfo();
        audioInfo.setUrl(audioMessage.getUrl());
        audioInfo.setTitle(audioMessage.getTitle());
        audioInfo.setThumbnailUrl(audioMessage.getThumbnailUrl());
        audioInfo.setDuration(audioMessage.getDuration());
        audioInfo.setVideoId(audioMessage.getVideoId());

        return audioInfo;
    }

    public static HtmlInfo toHtmlInfo(HtmlMessage htmlMessage) {
        HtmlInfo htmlInfo = new HtmlInfo();
        htmlInfo.setContent(htmlMessage.getContent());

        return htmlInfo;
    }

    public static AnswerInfo toAnswerInfo(AnswerMessage answerMessage) {
        AnswerInfo answerInfo = new AnswerInfo();

        if (answerMessage.hasAnswer()) {
            answerInfo.setAnswer(answerMessage.getAnswer().getValue());
        }

        if (answerMessage.hasAnsweredAt()) {
            answerInfo.setAnsweredAt(answerMessage.getAnsweredAt().getValue());
        }

        if (answerMessage.hasPublicVisible()) {
            answerInfo.setPublicVisible(answerMessage.getPublicVisible().getValue());
        }

        if (answerMessage.hasAnswerUserId()) {
            answerInfo.setAnswerUserId(answerMessage.getAnswerUserId().getValue());
        }

        answerInfo.setIgnored(false);
        return answerInfo;
    }

    public static PicturesMessage toPicturesMessage(List<MediaInfo> mediaInfos) {
        return PicturesMessage.newBuilder()
                .addAllPicture(
                        mediaInfos.stream()
                                .map(mediaInfo ->
                                        Picture.newBuilder()
                                                .setImgUrl(((PictureInfo) mediaInfo).getUrl())
                                                .setWidth(((PictureInfo) mediaInfo).getWidth())
                                                .setHeight(((PictureInfo) mediaInfo).getHeight())
                                                .setSize(((PictureInfo) mediaInfo).getSize())
                                                .build()
                                )
                                .collect(Collectors.toList())
                )
                .build();
    }

    public static VideoMessage toVideoMessage(MediaInfo mediaInfo) {
        VideoInfo videoInfo = (VideoInfo) mediaInfo;
        return VideoMessage.newBuilder()
                .setUrl(videoInfo.getUrl())
                .setTitle(videoInfo.getTitle())
                .setThumbnailUrl(videoInfo.getThumbnailUrl())
                .setDuration(videoInfo.getDuration())
                .setWidth(videoInfo.getWidth())
                .setHeight(videoInfo.getHeight())
                .setVideoId(videoInfo.getVideoId())
                .build();
    }

    public static AudioMessage toAudioMessage(MediaInfo mediaInfo) {
        AudioInfo audioInfo = (AudioInfo) mediaInfo;
        return AudioMessage.newBuilder()
                .setUrl(audioInfo.getUrl())
                .setTitle(audioInfo.getTitle())
                .setThumbnailUrl(audioInfo.getThumbnailUrl())
                .setDuration(audioInfo.getDuration())
                .setVideoId(audioInfo.getVideoId())
                .build();
    }

    public static HtmlMessage toHtmlMessage(MediaInfo mediaInfo) {
        HtmlInfo htmlInfo = (HtmlInfo) mediaInfo;
        return HtmlMessage.newBuilder()
                .setContent(htmlInfo.getContent())
                .build();
    }

    public static AnswerMessage toAnswerMessage(AnswerInfo answerInfo) {
        AnswerMessage.Builder builder = AnswerMessage.newBuilder();

        if (!StringUtils.isEmpty(answerInfo.getAnswer())) {
            builder.setAnswer(StringValue.of(answerInfo.getAnswer()));
        }
        if (Objects.nonNull(answerInfo.getPublicVisible())) {
            builder.setPublicVisible(BoolValue.of(answerInfo.getPublicVisible()));
        }
        if (!StringUtils.isEmpty(answerInfo.getAnsweredAt())) {
            builder.setAnsweredAt(Int64Value.of(answerInfo.getAnsweredAt()));
        }
        if (!StringUtils.isEmpty(answerInfo.getAnswerUserId())) {
            builder.setAnswerUserId(StringValue.of(answerInfo.getAnswerUserId()));
        }

        return builder.build();
    }
}
