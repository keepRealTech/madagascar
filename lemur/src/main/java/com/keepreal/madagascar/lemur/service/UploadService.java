package com.keepreal.madagascar.lemur.service;

import com.aliyun.oss.HttpMethod;
import com.aliyun.oss.OSS;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.keepreal.madagascar.lemur.config.OssClientConfiguration;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * Represents upload service.
 */
@Service
public class UploadService {

    private final OSS ossClient;
    private final String bucketName;
    private final Integer expireTimeInSeconds;

    /**
     * Constructs the upload service.
     *
     * @param ossClient     {@link OSS}.
     * @param configuration {@link OssClientConfiguration}.
     */
    public UploadService(OSS ossClient,
                         OssClientConfiguration configuration) {
        this.ossClient = ossClient;
        this.bucketName = configuration.getBucketName();
        this.expireTimeInSeconds = configuration.getExpireTimeInSeconds();
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

}
