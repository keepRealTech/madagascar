package com.keepreal.madagascar.lemur.controller;

import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.lemur.service.UploadService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import swagger.api.UploadApi;
import swagger.model.MediaUrlsRequest;
import swagger.model.MediaUrlsRequestV2;
import swagger.model.MultiMediaType;
import swagger.model.UploadUrlDTO;
import swagger.model.UploadUrlDTOV2;
import swagger.model.UploadUrlListResponse;
import swagger.model.UploadUrlListResponseV2;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
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

    /**
     * Constructs the upload controller.
     *
     * @param uploadService {@link UploadService}.
     */
    public UploadController(UploadService uploadService) {
        this.uploadService = uploadService;
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
    public ResponseEntity<UploadUrlListResponseV2> apiV2UploadMediaUrlsPost(@Valid MediaUrlsRequestV2 mediaUrlsRequestV2) {
        UploadUrlListResponseV2 response = new UploadUrlListResponseV2();
        UploadUrlDTOV2 data = new UploadUrlDTOV2();

        switch (mediaUrlsRequestV2.getMediaType()) {
            case PIC:
                if (mediaUrlsRequestV2.getFileNames().size() > 9 || mediaUrlsRequestV2.getFileNames().size() == 0) {
                    throw new KeepRealBusinessException(ErrorCode.REQUEST_IMAGE_NUMBER_TOO_LARGE);
                }
                List<UploadUrlDTO> uploadUrlDTOList = mediaUrlsRequestV2.getFileNames().stream().map(fileName -> {
                    UploadUrlDTO dto = new UploadUrlDTO();
                    String objectName = generatorObjectName(fileName);
                    dto.setObjectName(objectName);
                    dto.setUrl(uploadService.retrieveUploadUrl(objectName));
                    return dto;
                }).collect(Collectors.toList());
                data.setUrlList(uploadUrlDTOList);
                break;
            case VLOG:
            case MUSIC:
                data.setMediaInfo(uploadService.createUploadVideo(mediaUrlsRequestV2.getMediaTitle(), mediaUrlsRequestV2.getMediaFilename()));
                break;
            default:
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        response.setData(data);
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
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
