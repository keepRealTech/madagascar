package com.keepreal.madagascar.lemur.service;

import com.aliyun.oss.HttpMethod;
import com.aliyun.oss.OSS;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.aliyun.oss.model.MatchMode;
import com.aliyun.oss.model.PolicyConditions;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.vod.model.v20170321.CreateUploadVideoRequest;
import com.aliyuncs.vod.model.v20170321.CreateUploadVideoResponse;
import com.aliyuncs.vod.model.v20170321.GetPlayInfoRequest;
import com.aliyuncs.vod.model.v20170321.GetPlayInfoResponse;
import com.aliyuncs.vod.model.v20170321.RefreshUploadVideoRequest;
import com.aliyuncs.vod.model.v20170321.RefreshUploadVideoResponse;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.lemur.config.OssClientConfiguration;
import com.keepreal.madagascar.lemur.model.VideoInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import swagger.model.OssSignatureDTO;
import swagger.model.UploadMediaDTO;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

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
    private final String host;
    private final String accessKey;

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
        this.host = "https://" + bucketName + "." + "oss-cn-beijing.aliyuncs.com/";
        this.accessKey = configuration.getAccessKey();
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

    /**
     * retrieve videoId, uploadAddress, uploadAuth
     *
     * @param title    title.
     * @param filename filename.
     * @return {@link UploadMediaDTO}.
     */
    public UploadMediaDTO createUploadVideo(String title, String filename) {
        try {
            CreateUploadVideoRequest request = new CreateUploadVideoRequest();
            request.setTitle(title);
            request.setFileName(filename);
            CreateUploadVideoResponse acsResponse = client.getAcsResponse(request);

            UploadMediaDTO dto = new UploadMediaDTO();
            dto.setVideoId(acsResponse.getVideoId());
            dto.setUploadAddress(acsResponse.getUploadAddress());
            dto.setUploadAuth(acsResponse.getUploadAuth());

            return dto;
        } catch (ClientException e) {
            log.error(e.getLocalizedMessage());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }
    }

    /**
     * refresh uploadAddress, uploadAuth by videoId.
     *
     * @param videoId   videoId.
     * @return {@link UploadMediaDTO}.
     */
    public UploadMediaDTO refreshUploadVideo(String videoId) {
        try {
            RefreshUploadVideoRequest request = new RefreshUploadVideoRequest();
            request.setVideoId(videoId);

            RefreshUploadVideoResponse response = client.getAcsResponse(request);

            UploadMediaDTO dto = new UploadMediaDTO();
            dto.setVideoId(videoId);
            dto.setUploadAddress(response.getUploadAddress());
            dto.setUploadAuth(response.getUploadAuth());

            return dto;
        } catch (ClientException e) {
            log.error(e.getLocalizedMessage());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }
    }

    public VideoInfo retrieveVideoInfo(String videoId) {
        VideoInfo videoInfo = new VideoInfo();
        try {
            GetPlayInfoRequest request = new GetPlayInfoRequest();
            request.setVideoId(videoId);

            GetPlayInfoResponse response = client.getAcsResponse(request);
            List<GetPlayInfoResponse.PlayInfo> playInfoList = response.getPlayInfoList();
            if (playInfoList.size() == 0) {
                log.error("aliyun error! playInfoList is empty! video id is {}", videoId);
                throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
            } else {
                GetPlayInfoResponse.PlayInfo playInfo = playInfoList.get(0);
                videoInfo.setPlayURL(playInfo.getPlayURL());
                videoInfo.setWidth(playInfo.getWidth());
                videoInfo.setHeight(playInfo.getHeight());
                videoInfo.setDuration(playInfo.getDuration());
            }
            videoInfo.setCoverURL(response.getVideoBase().getCoverURL());
            videoInfo.setTitle(response.getVideoBase().getTitle());

        } catch (ClientException e) {
            log.error("aliyun error! videoId is {} message is {}", videoId, e.getLocalizedMessage());
        }
        return videoInfo;
    }

    public OssSignatureDTO retrieveOssSignature() {
        long expireEndTime = System.currentTimeMillis() + expireTimeInSeconds * 10 * 1000;
        Date expiration = new Date(expireEndTime);

        PolicyConditions policyConds = new PolicyConditions();
        policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, 1048576000);
        policyConds.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, "");

        String postPolicy = ossClient.generatePostPolicy(expiration, policyConds);
        byte[] binaryData = postPolicy.getBytes(StandardCharsets.UTF_8);
        String encodedPolicy = BinaryUtil.toBase64String(binaryData);
        String postSignature = ossClient.calculatePostSignature(postPolicy);

        OssSignatureDTO dto = new OssSignatureDTO();
        dto.setAccessid(this.accessKey);
        dto.setHost(this.host);
        dto.setPolicy(encodedPolicy);
        dto.setSignature(postSignature);
        dto.expire(expireEndTime / 1000);

        return dto;
    }

}
