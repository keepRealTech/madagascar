package com.keepreal.madagascar.lemur.service;

import com.aliyun.oss.HttpMethod;
import com.aliyun.oss.OSS;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.vod.model.v20170321.CreateUploadVideoRequest;
import com.aliyuncs.vod.model.v20170321.CreateUploadVideoResponse;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.lemur.config.OssClientConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import swagger.model.UploadMediaDTO;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * Represents upload service.
 */
@Service
@Slf4j
public class UploadService {

    private final OSS ossClient;
    private final String bucketName;
    private final Integer expireTimeInSeconds;
    private final DefaultAcsClient client;

    /**
     * Constructs the upload service.
     *
     * @param ossClient     {@link OSS}.
     * @param configuration {@link OssClientConfiguration}.
     * @param client        {@link DefaultAcsClient}.
     */
    public UploadService(OSS ossClient,
                         OssClientConfiguration configuration,
                         DefaultAcsClient client) {
        this.ossClient = ossClient;
        this.bucketName = configuration.getBucketName();
        this.expireTimeInSeconds = configuration.getExpireTimeInSeconds();
        this.client = client;
    }

    /**
     * retrieve upload url
     *
     * @param objectName object name.
     * @return upload url.
     */
    public String retrieveUploadUrl(String objectName) {
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, objectName, HttpMethod.PUT);
        LocalDateTime localDateTime = LocalDateTime.now().plusSeconds(expireTimeInSeconds);
        Date date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        request.setExpiration(date);
        request.setContentType("application/octet-stream");
        return ossClient.generatePresignedUrl(request).toString();
    }

    public UploadMediaDTO createUploadVideo(String title, String filename) {
        try {
            CreateUploadVideoRequest request = new CreateUploadVideoRequest();
            request.setTitle(title);
            request.setFileName(filename);
            CreateUploadVideoResponse acsResponse = client.getAcsResponse(request);

            UploadMediaDTO dto = new UploadMediaDTO();
            dto.setVedioId(acsResponse.getVideoId());
            dto.setUploadAddress(acsResponse.getUploadAddress());
            dto.setUploadAuth(acsResponse.getUploadAuth());

            return dto;
        } catch (ClientException e) {
            log.error(e.getLocalizedMessage());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }
    }

}
