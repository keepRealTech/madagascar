package com.keepreal.madagascar.lemur.service;

import com.keepreal.madagascar.common.AudioMessage;
import com.keepreal.madagascar.common.HtmlMessage;
import com.keepreal.madagascar.common.Picture;
import com.keepreal.madagascar.common.PicturesMessage;
import com.keepreal.madagascar.common.VideoMessage;
import com.keepreal.madagascar.lemur.config.OssClientConfiguration;
import com.keepreal.madagascar.lemur.model.VideoInfo;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import swagger.model.MultiMediaDTO;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class MediaService {

    private static final long MILLISECONDS = 1000L;
    private final String host;

    private final UploadService uploadService;

    public MediaService(UploadService uploadService,
                        OssClientConfiguration clientConfiguration) {
        this.uploadService = uploadService;
        this.host = clientConfiguration.getOssPrefix();
    }

    public VideoMessage videoMessage(MultiMediaDTO multiMediaDTO) {
        if (Objects.isNull(multiMediaDTO)) {
            return null;
        }

        String videoId = multiMediaDTO.getVideoId();

        VideoInfo videoInfo = this.uploadService.retrieveVideoInfo(videoId);
        return VideoMessage.newBuilder()
                .setUrl(videoInfo.getPlayURL())
                .setTitle(this.processTitle(videoInfo.getTitle()))
                .setThumbnailUrl(this.getThumbnailUrl(multiMediaDTO, videoInfo))
                .setDuration(this.toMilliseconds(videoInfo.getDuration()))
                .setWidth(videoInfo.getWidth())
                .setHeight(videoInfo.getHeight())
                .setVideoId(videoId)
                .build();
    }

    public AudioMessage audioMessage(MultiMediaDTO multiMediaDTO) {
        if (Objects.isNull(multiMediaDTO)) {
            return null;
        }

        String videoId = multiMediaDTO.getVideoId();

        VideoInfo videoInfo = this.uploadService.retrieveVideoInfo(videoId);

        return AudioMessage.newBuilder()
                .setUrl(videoInfo.getPlayURL())
                .setTitle(this.processTitle(videoInfo.getTitle()))
                .setThumbnailUrl(this.getThumbnailUrl(multiMediaDTO, videoInfo))
                .setDuration(this.toMilliseconds(videoInfo.getDuration()))
                .setVideoId(videoId)
                .build();
    }

    public PicturesMessage picturesMessage(List<MultiMediaDTO> multiMediaDTOList) {
        if (Objects.isNull(multiMediaDTOList)) {
            return null;
        }

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
        if (Objects.isNull(multiMediaDTO)) {
            return null;
        }

        return HtmlMessage.newBuilder()
                .setContent(multiMediaDTO.getContent())
                .build();
    }

    private long toMilliseconds(String duration) {
        return (long) (Float.parseFloat(duration) * MILLISECONDS);
    }

    private String getThumbnailUrl(MultiMediaDTO multiMediaDTO, VideoInfo videoInfo) {
        return StringUtils.isEmpty(multiMediaDTO.getThumbnailUrl()) ? videoInfo.getCoverURL() : this.host + multiMediaDTO.getThumbnailUrl();
    }

    private String processTitle(String title) {
        return title.substring(0, title.lastIndexOf('.'));
    }

}
