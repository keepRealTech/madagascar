package com.keepreal.madagascar.lemur.service;

import com.keepreal.madagascar.fossa.AudioMessage;
import com.keepreal.madagascar.fossa.HtmlMessage;
import com.keepreal.madagascar.fossa.Picture;
import com.keepreal.madagascar.fossa.PicturesMessage;
import com.keepreal.madagascar.fossa.VideoMessage;
import org.springframework.stereotype.Service;
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


        return VideoMessage.newBuilder()
//                .setUrl()
//                .setThumbnailUrl()
//                .setDuration()
//                .setWidth()
//                .setHeight()
                .setVideoId(videoId)
                .build();
    }

    public AudioMessage audioMessage(MultiMediaDTO multiMediaDTO) {
        String videoId = multiMediaDTO.getVideoId();

        return AudioMessage.newBuilder()
//                .setUrl()
//                .setThumbnailUrl()
//                .setDuration()
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
}
