package com.keepreal.madagascar.lemur.service;

import com.keepreal.madagascar.common.AudioMessage;
import com.keepreal.madagascar.common.HtmlMessage;
import com.keepreal.madagascar.common.Picture;
import com.keepreal.madagascar.common.PicturesMessage;
import com.keepreal.madagascar.common.VideoMessage;
import com.keepreal.madagascar.lemur.model.VideoInfo;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import swagger.model.MultiMediaDTO;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MediaService {

    private static long MILLISECONDS = 1000L;

    private final UploadService uploadService;

    public MediaService(UploadService uploadService) {
        this.uploadService = uploadService;
    }

    public VideoMessage videoMessage(MultiMediaDTO multiMediaDTO) {
        String videoId = multiMediaDTO.getVideoId();

        VideoInfo videoInfo = uploadService.retrieveVideoInfo(videoId);
        return VideoMessage.newBuilder()
                .setUrl(videoInfo.getPlayURL())
                .setThumbnailUrl(this.getThumbnailUrl(multiMediaDTO, videoInfo))
                .setDuration(this.toMilliseconds(videoInfo.getDuration()))
                .setWidth(videoInfo.getWidth())
                .setHeight(videoInfo.getHeight())
                .setVideoId(videoId)
                .build();
    }

    public AudioMessage audioMessage(MultiMediaDTO multiMediaDTO) {
        String videoId = multiMediaDTO.getVideoId();

        VideoInfo videoInfo = uploadService.retrieveVideoInfo(videoId);

        return AudioMessage.newBuilder()
                .setUrl(videoInfo.getPlayURL())
                .setThumbnailUrl(this.getThumbnailUrl(multiMediaDTO, videoInfo))
                .setDuration(this.toMilliseconds(videoInfo.getDuration()))
                .setVideoId(videoId)
                .build();
    }

    public PicturesMessage picturesMessage(List<MultiMediaDTO> multiMediaDTOList) {
        return PicturesMessage.newBuilder()
                .addAllPicture(
                        multiMediaDTOList.stream()
                                .map(dto -> Picture.newBuilder()
                                        .setImgUrl(dto.getUrl())
                                        .setWidth(dto.getWidth())
                                        .setHeight(dto.getHeight())
                                        .setSize(dto.getSize())
                                        .build())
                                .collect(Collectors.toList()))
                .build();
    }

    public HtmlMessage htmlMessage(MultiMediaDTO multiMediaDTO) {
        return HtmlMessage.newBuilder()
                .setContent(multiMediaDTO.getContent())
                .build();
    }

    private long toMilliseconds(String duration) {
        return (long) (Float.valueOf(duration) * MILLISECONDS);
    }

    private String getThumbnailUrl(MultiMediaDTO multiMediaDTO, VideoInfo videoInfo) {
        return StringUtils.isEmpty(multiMediaDTO.getThumbnailUrl()) ? videoInfo.getCoverURL() : multiMediaDTO.getThumbnailUrl();
    }
}
