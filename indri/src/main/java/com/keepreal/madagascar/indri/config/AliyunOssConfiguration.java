package com.keepreal.madagascar.indri.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Represents the aliyun oss configurations.
 */
@Configuration
@ConfigurationProperties(prefix = "aliyun-oss")
@Data
public class AliyunOssConfiguration {

    private String endpoint;
    private String accessKey;
    private String accessSecret;
    private String bucketName;

    /**
     * Constructs the oss client bean.
     *
     * @return OSS client {@link OSS}.
     */
    @Bean
    public OSS getOssClient() {
        return new OSSClientBuilder().build(
                this.endpoint, this.accessKey, this.accessSecret);
    }

}