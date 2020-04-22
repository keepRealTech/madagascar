package com.keepreal.madagascar.indri.service;

import com.aliyun.oss.OSS;
import com.keepreal.madagascar.Indri.CommonStatus;
import com.keepreal.madagascar.Indri.ReactorImageServiceGrpc;
import com.keepreal.madagascar.Indri.UploadImagesRequest;
import com.keepreal.madagascar.error.ErrorCode;
import com.keepreal.madagascar.indri.config.AliyunOssConfiguration;
import com.keepreal.madagascar.indri.util.CommonStatusUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayInputStream;
import java.util.AbstractMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Represents the image service logic.
 */
@Service
public class ImageService extends ReactorImageServiceGrpc.ImageServiceImplBase {

    private final OSS ossClient;
    private final String bucketName;
    private final CommonStatusUtils commonStatusUtils;

    /**
     * Constructs the image service overriding grpc with reactive behavior.
     *
     * @param ossClient              Aliyun oss client.
     * @param aliyunOssConfiguration Aliyun oss configuration.
     * @param commonStatusUtils      Common status utils.
     */
    public ImageService(OSS ossClient,
                        AliyunOssConfiguration aliyunOssConfiguration,
                        CommonStatusUtils commonStatusUtils) {
        this.ossClient = ossClient;
        this.bucketName = aliyunOssConfiguration.getBucketName();
        this.commonStatusUtils = commonStatusUtils;
    }

    /**
     * Overrides the reactor grpc upload image stub.
     *
     * @param request {@link UploadImagesRequest}.
     * @return {@link CommonStatus}.
     */
    @Override
    public Mono<CommonStatus> uploadImages(Mono<UploadImagesRequest> request) {
        return request.flatMapMany(uploadImagesRequest ->
                    Flux.fromIterable(IntStream.range(0, uploadImagesRequest.getImageNamesCount())
                        .mapToObj(i -> new AbstractMap.SimpleEntry<>(
                                uploadImagesRequest.getImageNames(i),
                                uploadImagesRequest.getImageContent(i).toByteArray()))
                        .collect(Collectors.toList())))
                .publishOn(Schedulers.elastic())
                .flatMap(simpleEntry ->
                        Mono.just(this.ossClient.putObject(
                                this.bucketName,
                                simpleEntry.getKey(),
                                new ByteArrayInputStream(simpleEntry.getValue()))))
                .filter(putObjectResult -> 200 != putObjectResult.getResponse().getStatusCode())
                .next()
                .map(putObjectResult -> this.commonStatusUtils.buildCommonStatus(ErrorCode.GRPC_IMAGE_UPLOAD_ERROR))
                .switchIfEmpty(Mono.just(this.commonStatusUtils.buildCommonStatus(ErrorCode.GRPC_SUCC)))
                .onErrorReturn(this.commonStatusUtils.buildCommonStatus(ErrorCode.GRPC_IMAGE_UPLOAD_ERROR));
    }

}
