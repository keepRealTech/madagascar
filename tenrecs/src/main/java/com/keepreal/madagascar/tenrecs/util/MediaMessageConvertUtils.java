package com.keepreal.madagascar.tenrecs.util;

import com.keepreal.madagascar.common.AudioMessage;
import com.keepreal.madagascar.common.FeedMessage;
import com.keepreal.madagascar.common.HtmlMessage;
import com.keepreal.madagascar.common.MediaType;
import com.keepreal.madagascar.common.Picture;
import com.keepreal.madagascar.common.PicturesMessage;
import com.keepreal.madagascar.common.VideoMessage;
import com.keepreal.madagascar.tenrecs.model.AudioInfo;
import com.keepreal.madagascar.tenrecs.model.Feed;
import com.keepreal.madagascar.tenrecs.model.HtmlInfo;
import com.keepreal.madagascar.tenrecs.model.MediaInfo;
import com.keepreal.madagascar.tenrecs.model.PictureInfo;
import com.keepreal.madagascar.tenrecs.model.VideoInfo;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
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

    /**
     * Converts {@link FeedMessage} into {@link List<MediaInfo>}.
     *
     * @param feedMessage   {@link FeedMessage}.
     * @return  {@link List<MediaInfo>}
     */
    public static List<MediaInfo> buildMediaInfos(FeedMessage feedMessage) {
        List<MediaInfo> mediaInfos = new ArrayList<>();
        switch (feedMessage.getType()) {
            case MEDIA_PICS:
            case MEDIA_ALBUM:
                mediaInfos.addAll(toPictureInfoList(feedMessage.getPics()));
                break;
            case MEDIA_VIDEO:
                mediaInfos.add(toVideoInfo(feedMessage.getVideo()));
                break;
            case MEDIA_AUDIO:
                mediaInfos.add(toAudioInfo(feedMessage.getAudio()));
                break;
            case MEDIA_HTML:
                mediaInfos.add(toHtmlInfo(feedMessage.getHtml()));
                break;
        }
        return mediaInfos;
    }

    public static void processMedia(FeedMessage.Builder builder, Feed feedInfo) {
        if (feedInfo.getMultiMediaType() == null) {
            if (CollectionUtils.isEmpty(feedInfo.getImageUris())) {
                builder.setType(MediaType.MEDIA_TEXT);
            } else {
                List<String> imageUrls = feedInfo.getImageUris();
                builder.setType(MediaType.MEDIA_PICS);
                builder.setPics(PicturesMessage.newBuilder()
                        .addAllPicture(
                                imageUrls.stream()
                                        .map(url -> Picture.newBuilder()
                                                .setImgUrl(url)
                                                .setHeight(0)
                                                .setWidth(0)
                                                .setSize(0)
                                                .build())
                                        .collect(Collectors.toList())).build());
            }
            return;
        }
        MediaType mediaType = MediaType.valueOf(feedInfo.getMultiMediaType());
        builder.setType(mediaType);
        switch (mediaType) {
            case MEDIA_PICS:
            case MEDIA_ALBUM:
                builder.setPics(toPicturesMessage(feedInfo.getMediaInfos()));
                break;
            case MEDIA_VIDEO:
                builder.setVideo(toVideoMessage(feedInfo.getMediaInfos().get(0)));
                break;
            case MEDIA_AUDIO:
                builder.setAudio(toAudioMessage(feedInfo.getMediaInfos().get(0)));
                break;
            case MEDIA_HTML:
                builder.setHtml(toHtmlMessage(feedInfo.getMediaInfos().get(0)));
                break;
        }
    }
}
