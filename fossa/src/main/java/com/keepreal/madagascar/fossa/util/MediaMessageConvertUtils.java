package com.keepreal.madagascar.fossa.util;

import com.keepreal.madagascar.common.AudioMessage;
import com.keepreal.madagascar.common.HtmlMessage;
import com.keepreal.madagascar.common.PicturesMessage;
import com.keepreal.madagascar.common.VideoMessage;
import com.keepreal.madagascar.fossa.model.AudioInfo;
import com.keepreal.madagascar.fossa.model.HtmlInfo;
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

    public static List<PictureInfo> pictureInfoList(PicturesMessage picturesMessage) {
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

    public static VideoInfo videoInfo(VideoMessage videoMessage) {
        VideoInfo videoInfo = new VideoInfo();
        videoInfo.setUrl(videoMessage.getUrl());
        videoInfo.setThumbnailUrl(videoMessage.getThumbnailUrl());
        videoInfo.setDuration(videoInfo.getDuration());
        videoInfo.setWidth(videoMessage.getWidth());
        videoInfo.setHeight(videoMessage.getHeight());
        videoInfo.setVideoId(videoMessage.getVideoId());

        return videoInfo;
    }

    public static AudioInfo audioInfo(AudioMessage audioMessage) {
        AudioInfo audioInfo = new AudioInfo();
        audioInfo.setUrl(audioMessage.getUrl());
        audioInfo.setThumbnailUrl(audioMessage.getThumbnailUrl());
        audioInfo.setDuration(audioMessage.getDuration());
        audioInfo.setVideoId(audioMessage.getVideoId());

        return audioInfo;
    }

    public static HtmlInfo htmlInfo(HtmlMessage htmlMessage) {
        HtmlInfo htmlInfo = new HtmlInfo();
        htmlInfo.setContent(htmlMessage.getContent());

        return htmlInfo;
    }
}
