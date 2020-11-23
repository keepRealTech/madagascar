package com.keepreal.madagascar.lemur.controller;

import com.keepreal.madagascar.common.FeedMessage;
import com.keepreal.madagascar.common.VideoMessage;
import com.keepreal.madagascar.common.constants.Constants;
import com.keepreal.madagascar.common.constants.Templates;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.lemur.model.TranscodeComplete;
import com.keepreal.madagascar.lemur.model.VideoInfo;
import com.keepreal.madagascar.lemur.service.ChatService;
import com.keepreal.madagascar.lemur.service.FeedService;
import com.keepreal.madagascar.lemur.service.MediaService;
import com.keepreal.madagascar.lemur.service.UploadService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import swagger.api.UploadApi;
import swagger.model.MediaUrlRequest;
import swagger.model.MediaUrlsRequest;
import swagger.model.OssSignatureDTO;
import swagger.model.OssSignatureResponse;
import swagger.model.RefreshVideoResponse;
import swagger.model.UploadMediaDTO;
import swagger.model.UploadUrlDTO;
import swagger.model.UploadUrlListResponse;
import swagger.model.UploadUrlResponse;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Represents the upload controller.
 */
@RestController
public class UploadController implements UploadApi {

    private final UploadService uploadService;
    private final FeedService feedService;
    private final MediaService mediaService;
    private final ChatService chatService;

    /**
     * Constructs the upload controller.
     *
     * @param uploadService {@link UploadService}.
     * @param feedService   {@link FeedService}.
     * @param mediaService  {@link MediaService}.
     * @param chatService   {@link ChatService}.
     */
    public UploadController(UploadService uploadService,
                            FeedService feedService,
                            MediaService mediaService,
                            ChatService chatService) {
        this.uploadService = uploadService;
        this.feedService = feedService;
        this.mediaService = mediaService;
        this.chatService = chatService;
    }

    /**
     * Implements the media urls api.
     *
     * @param mediaUrlsRequest {@link MediaUrlsRequest}.
     * @return {@link UploadUrlListResponse}.
     */
    @Override
    public ResponseEntity<UploadUrlListResponse> apiV1UploadMediaUrlsPost(MediaUrlsRequest mediaUrlsRequest) {

        if (mediaUrlsRequest.getFileNames().size() > 9 || mediaUrlsRequest.getFileNames().size() == 0) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_IMAGE_NUMBER_TOO_LARGE);
        }

        List<UploadUrlDTO> uploadUrlDTOList = mediaUrlsRequest.getFileNames().stream().map(fileName -> {
            UploadUrlDTO dto = new UploadUrlDTO();
            String objectName = generatorObjectName(fileName);
            dto.setObjectName(objectName);
            dto.setUrl(uploadService.retrieveUploadUrl(objectName));
            return dto;
        }).collect(Collectors.toList());

        UploadUrlListResponse response = new UploadUrlListResponse();
        response.data(uploadUrlDTOList);
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<UploadUrlResponse> apiV1UploadMediaUrlPost(@Valid MediaUrlRequest mediaUrlRequest) {
        UploadMediaDTO uploadVideo = uploadService.createUploadVideo(mediaUrlRequest.getMediaTitle(), mediaUrlRequest.getMediaFilename());

        UploadUrlResponse response = new UploadUrlResponse();
        response.setData(uploadVideo);
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<RefreshVideoResponse> apiV1UploadRefreshVideoGet(@NotNull @Valid String videoId) {
        UploadMediaDTO dto = uploadService.refreshUploadVideo(videoId);

        RefreshVideoResponse response = new RefreshVideoResponse();
        response.setData(dto);
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<OssSignatureResponse> apiV1UploadOssSignaturePost(@Valid MediaUrlsRequest mediaUrlsRequest) {
        OssSignatureDTO dto = uploadService.retrieveOssSignature();
        dto.setObjectNames(mediaUrlsRequest.getFileNames().stream().map(this::generatorObjectName).collect(Collectors.toList()));

        OssSignatureResponse response = new OssSignatureResponse();
        response.setData(dto);
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/upload/transCode/callback")
    public void callback(@RequestBody TranscodeComplete transcodeComplete) {
        String videoId = transcodeComplete.getVideoId();
        VideoInfo videoInfo = uploadService.retrieveVideoInfo(videoId);

        FeedMessage feedMessage = this.feedService.updateFeedByVideoId(videoId, VideoMessage.newBuilder()
                .setUrl(videoInfo.getPlayURL())
                .setTitle(this.mediaService.processTitle(videoInfo.getTitle()))
                .setThumbnailUrl(videoInfo.getCoverURL())
                .setDuration(this.mediaService.toMilliseconds(videoInfo.getDuration()))
                .setWidth(videoInfo.getWidth())
                .setHeight(videoInfo.getHeight())
                .setVideoId(videoId)
                .build());

        SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");
        String timeStr = format.format(new Date(feedMessage.getCreatedAt()));
        this.chatService.sendMessage(Constants.OFFICIAL_USER_ID, Collections.singletonList(feedMessage.getUserId()), String.format(Templates.TRANS_CODE_COMPLETE, timeStr), 0);
    }

    /**
     * generator object name by file name(random generator by uuid).
     *
     * @param fileName file name.
     * @return object name.
     */
    private String generatorObjectName(String fileName) {
        String extension = Objects.requireNonNull(fileName).substring(fileName.lastIndexOf("."));
        String randomName = UUID.randomUUID().toString().replace("-", "");
        return randomName + extension;
    }

}
