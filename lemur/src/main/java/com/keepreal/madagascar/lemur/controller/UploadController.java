package com.keepreal.madagascar.lemur.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import swagger.api.UploadApi;
import swagger.model.UploadUrlListResponse;
import swagger.model.UploadUrlResponse;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Represents the upload controller.
 */
@RestController
public class UploadController implements UploadApi {

    /**
     * Implements the get image urls api.
     *
     * @param fileNameList fileNameList (required).
     * @return {@link UploadUrlListResponse}.
     */
    @Override
    public ResponseEntity<UploadUrlListResponse> apiV1UploadGetImageUrlsGet(@NotNull @Valid List<String> fileNameList) {
        return null;
    }

    /**
     * Implements the  get video url api.
     *
     * @param fileName fileName (required).
     * @return  {@link UploadUrlResponse}.
     */
    @Override
    public ResponseEntity<UploadUrlResponse> apiV1UploadGetVideoUrlGet(@NotNull @Valid String fileName) {
        return null;
    }
}
