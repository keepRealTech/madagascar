package com.keepreal.madagascar.fossa.util;

import com.google.protobuf.BoolValue;
import com.google.protobuf.Int64Value;
import com.google.protobuf.StringValue;
import com.keepreal.madagascar.common.AudioMessage;
import com.keepreal.madagascar.common.HtmlMessage;
import com.keepreal.madagascar.common.Picture;
import com.keepreal.madagascar.common.PicturesMessage;
import com.keepreal.madagascar.common.QuestionMessage;
import com.keepreal.madagascar.common.VideoMessage;
import com.keepreal.madagascar.fossa.model.AudioInfo;
import com.keepreal.madagascar.fossa.model.HtmlInfo;
import com.keepreal.madagascar.fossa.model.MediaInfo;
import com.keepreal.madagascar.fossa.model.PictureInfo;
import com.keepreal.madagascar.fossa.model.QuestionInfo;
import com.keepreal.madagascar.fossa.model.VideoInfo;
import org.springframework.util.StringUtils;

import java.util.List;
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

    public static QuestionInfo toQuestionInfo(QuestionMessage questionMessage) {
        QuestionInfo questionInfo = new QuestionInfo();
        questionInfo.setText(questionMessage.getText());
        if (questionMessage.hasPriceInCents()) {
            questionInfo.setPriceInCents(questionMessage.getPriceInCents().getValue());
        }
        if (questionMessage.hasQuestionSkuId()) {
            questionInfo.setQuestionSkuId(questionMessage.getQuestionSkuId().getValue());
        }
        if (questionMessage.hasReceipt()) {
            questionInfo.setReceipt(questionMessage.getReceipt().getValue());
        }
        if (questionMessage.hasTransactionId()) {
            questionInfo.setTransactionId(questionMessage.getTransactionId().getValue());
        }
        questionInfo.setAnswerUserId(questionMessage.getAnswerUserId());
        questionInfo.setAnswerAt(questionMessage.getAnsweredAt());
        return questionInfo;
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

    public static QuestionMessage toQuestionMessage(MediaInfo mediaInfo) {
        QuestionInfo questionInfo = (QuestionInfo) mediaInfo;

        QuestionMessage.Builder builder = QuestionMessage.newBuilder()
                .setText(questionInfo.getText())
                .setAnswerUserId(questionInfo.getAnswerUserId())
                .setAnsweredAt(questionInfo.getAnswerAt());

        if (questionInfo.getPriceInCents() != null) {
            builder.setPriceInCents(Int64Value.of(questionInfo.getPriceInCents()));
        }
        if (!StringUtils.isEmpty(questionInfo.getQuestionSkuId())) {
            builder.setQuestionSkuId(StringValue.of(questionInfo.getQuestionSkuId()));
        }
        if (!StringUtils.isEmpty(questionInfo.getReceipt())) {
            builder.setReceipt(StringValue.of(questionInfo.getReceipt()));
        }
        if (!StringUtils.isEmpty(questionInfo.getTransactionId())) {
            builder.setTransactionId(StringValue.of(questionInfo.getTransactionId()));
        }
        if (!StringUtils.isEmpty(questionInfo.getAnswer())) {
            builder.setAnswer(StringValue.of(questionInfo.getAnswer()));
        }
        if (questionInfo.getPublicVisible() != null) {
            builder.setPublicVisible(BoolValue.of(questionInfo.getPublicVisible()));
        }

        return builder.build();
    }
}
