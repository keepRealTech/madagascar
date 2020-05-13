package com.keepreal.madagascar.indri.service;

import com.aliyun.oss.OSS;
import com.keepreal.madagascar.common.CommonStatus;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.indri.ReactorImageServiceGrpc;
import com.keepreal.madagascar.indri.UploadImagesRequest;
import com.keepreal.madagascar.indri.config.AliyunOssConfiguration;
import com.keepreal.madagascar.indri.util.CommonStatusUtils;
import lombok.extern.slf4j.Slf4j;
import org.lognet.springboot.grpc.GRpcService;
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
@GRpcService
@Slf4j
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
                .flatMap(simpleEntry -> Mono.just(this.ossClient.putObject(
                        this.bucketName,
                        simpleEntry.getKey(),
                        new ByteArrayInputStream(simpleEntry.getValue()))))
                .last()
                .map(putObjectResult -> this.commonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC))
                .onErrorReturn(this.commonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_GRPC_IMAGE_UPLOAD_ERROR))
                .doOnError(error -> log.error(error.toString()));
    }

}
