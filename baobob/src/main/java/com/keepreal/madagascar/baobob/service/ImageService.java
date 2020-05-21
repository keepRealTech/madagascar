package com.keepreal.madagascar.baobob.service;

import com.google.protobuf.ByteString;
import com.keepreal.madagascar.common.CommonStatus;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.indri.ImageServiceGrpc;
import com.keepreal.madagascar.indri.UploadImagesRequest;
import io.grpc.Channel;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
     * @param image Image.
     * @return The image uri.
     */
    public Mono<Void> migrateSingleImage(String url) throws IOException {

        URL imageUrl = new URL(url);
        ReadableByteChannel readableByteChannel = Channels.newChannel(imageUrl.openStream());


        ImageServiceGrpc.ImageServiceBlockingStub stub = ImageServiceGrpc.newBlockingStub(this.channel);


        CommonStatus response;
        try {
            UploadImagesRequest request = UploadImagesRequest.newBuilder()
                    .addImageNames(uri)
                    .addImageContent(ByteString.copyFrom(image.getBytes()))
                    .build();
            response = stub.uploadImages(request);
        } catch (StatusRuntimeException | IOException e) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_GRPC_IMAGE_UPLOAD_ERROR);
        }

        if (Objects.isNull(response) || ErrorCode.REQUEST_SUCC_VALUE != response.getRtn()) {
            log.error("Upload image returned null.");
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        return uri;
    }

}
