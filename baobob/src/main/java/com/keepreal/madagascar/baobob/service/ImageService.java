package com.keepreal.madagascar.baobob.service;

import com.google.protobuf.ByteString;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.indri.MigrateImageRequest;
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
     * Migrates a single image, returns the uri.
     *
     * @param sourceUrl Image source url.
     * @return The image uri.
     */
    public Mono<String> migrateSingleImage(String sourceUrl) {
        ReactorImageServiceGrpc.ReactorImageServiceStub stub = ReactorImageServiceGrpc.newReactorStub(this.channel);

        String destinationUri = this.buildImageUri();

        MigrateImageRequest request = MigrateImageRequest.newBuilder()
                .setSourceUrl(sourceUrl)
                .setDestinationUri(destinationUri)
                .build();

        return stub.migrateImage(request)
                .map(commonStatus -> {
                    if (ErrorCode.REQUEST_SUCC_VALUE != commonStatus.getRtn()) {
                        log.error("Migrate image failed with {}", commonStatus.toString());
                        return "";
                    }
                    return destinationUri;
                })
                .onErrorReturn("")
                .doOnError(err -> log.error("Migrate image failed with {}", err.toString()));
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