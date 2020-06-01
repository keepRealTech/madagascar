package com.keepreal.madagascar.lemur.controller;

import com.keepreal.madagascar.lemur.service.UploadService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import swagger.api.UploadApi;
import swagger.model.MediaUrlsRequest;
import swagger.model.UploadUrlDTO;
import swagger.model.UploadUrlListResponse;

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
     * @param mediaUrlsRequest  {@link MediaUrlsRequest}.
     * @return  {@link UploadUrlListResponse}.
     */
    @Override
    public ResponseEntity<UploadUrlListResponse> apiV1UploadMediaUrlsPost(MediaUrlsRequest mediaUrlsRequest) {
        List<UploadUrlDTO> uploadUrlDTOList = mediaUrlsRequest.getFileNames().stream().map(fileName -> {
            UploadUrlDTO dto = new UploadUrlDTO();
            String objectName = generatorObjectName(fileName);
            dto.setObjectName(objectName);
            dto.setUrl(uploadService.retrieveUploadUrl(objectName));
            return dto;
        }).collect(Collectors.toList());

        UploadUrlListResponse response = new UploadUrlListResponse();
        response.data(uploadUrlDTOList);
        return null;
    }

    /**
     * generator object name by file name(random generator by uuid).
     *
     * @param fileName  file name.
     * @return  object name.
     */
    private String generatorObjectName(String fileName) {
        String extension = Objects.requireNonNull(fileName).substring(fileName.lastIndexOf("."));
        String randomName = UUID.randomUUID().toString().replace("-", "");
        return randomName + extension;
    }
}
