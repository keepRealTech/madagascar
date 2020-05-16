package com.keepreal.madagascar.lemur.service;

import com.google.protobuf.ByteString;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.indri.ImageServiceGrpc;
import com.keepreal.madagascar.indri.UploadImagesRequest;
import com.keepreal.madagascar.lemur.util.ImageUtils;
import io.grpc.Channel;
import io.grpc.StatusRuntimeException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;

/**
 * Represents the upload image service.
 */
@Service
public class ImageService {

    private final Channel channel;

    /**
     * Constructs the image service.
     *
     * @param channel GRpc managed channel connection to service Indri.
     */
    public ImageService(@Qualifier("indriChannel") Channel channel) {
        this.channel = channel;
    }

    /**
     * Uploads a single image async, returns the uri instantly no matter it succeeds or not.
     *
     * @param image Image.
     * @return The image uri.
     */
    public String uploadSingleImageAsync(MultipartFile image) {
        ImageServiceGrpc.ImageServiceFutureStub stub = ImageServiceGrpc.newFutureStub(this.channel);

        String extension = Objects.requireNonNull(image.getOriginalFilename())
                .substring(image.getOriginalFilename().lastIndexOf("."));
        String uri = ImageUtils.buildImageUri() + extension;

        UploadImagesRequest request = null;
        try {
            request = UploadImagesRequest.newBuilder()
                    .addImageNames(uri)
                    .addImageContent(ByteString.copyFrom(image.getBytes()))
                    .build();
            stub.uploadImages(request);
        } catch (StatusRuntimeException | IOException e) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_GRPC_IMAGE_UPLOAD_ERROR);
        }

        return uri;
    }

}
