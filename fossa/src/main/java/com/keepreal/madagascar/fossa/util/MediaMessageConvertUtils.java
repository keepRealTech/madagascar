package com.keepreal.madagascar.fossa.util;

import com.keepreal.madagascar.common.AudioMessage;
import com.keepreal.madagascar.common.HtmlMessage;
import com.keepreal.madagascar.common.Picture;
import com.keepreal.madagascar.common.PicturesMessage;
import com.keepreal.madagascar.common.VideoMessage;
import com.keepreal.madagascar.fossa.model.AudioInfo;
import com.keepreal.madagascar.fossa.model.HtmlInfo;
import com.keepreal.madagascar.fossa.model.MediaInfo;
import com.keepreal.madagascar.fossa.model.PictureInfo;
import com.keepreal.madagascar.fossa.model.VideoInfo;

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
        videoInfo.setThumbnailUrl(videoMessage.getThumbnailUrl());
        videoInfo.setDuration(videoInfo.getDuration());
        videoInfo.setWidth(videoMessage.getWidth());
        videoInfo.setHeight(videoMessage.getHeight());
        videoInfo.setVideoId(videoMessage.getVideoId());

        return videoInfo;
    }

    public static AudioInfo toAudioInfo(AudioMessage audioMessage) {
        AudioInfo audioInfo = new AudioInfo();
        audioInfo.setUrl(audioMessage.getUrl());
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
}
