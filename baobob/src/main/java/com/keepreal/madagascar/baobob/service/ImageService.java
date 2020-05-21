package com.keepreal.madagascar.baobob.service;

import com.google.protobuf.ByteString;
import com.keepreal.madagascar.indri.ReactorImageServiceGrpc;
import com.keepreal.madagascar.indri.UploadImagesRequest;
import io.grpc.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Represents the upload image service.
 */
@Service
@Slf4j
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
     * @param image Image content.
     * @return The image uri.
     */
    public Mono<String> uploadSingleImage(byte[] image) {
        ReactorImageServiceGrpc.ReactorImageServiceStub stub = ReactorImageServiceGrpc.newReactorStub(this.channel);

        String uri = this.buildImageUri();

        UploadImagesRequest request = UploadImagesRequest.newBuilder()
                .addImageNames(uri)
                .addImageContent(ByteString.copyFrom(image))
                .build();

        return stub.uploadImages(request)
                .thenReturn(uri)
                .onErrorReturn("")
                .doOnError(err -> log.error(err.toString()));
    }

    /**
     * Generates a new random image uri.
     *
     * @return Uri.
     */
    private String buildImageUri() {
        return UUID.randomUUID().toString().replace("-", "") + ".jpg";
    }

}
