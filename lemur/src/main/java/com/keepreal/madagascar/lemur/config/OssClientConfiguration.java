package com.keepreal.madagascar.lemur.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "aliyun-oss")
@Data
public class OssClientConfiguration {

    private String accessKey;
    private String accessSecret;
    private String bucketName;
    private Integer expireTimeInSeconds;
    private String ossEndpoint;
    private String ossPrefix;
    private String roleArn = "acs:ram::1398284016177859:role/aliyunossclientdefaultrole";
    private String stsEndpoint = "sts.aliyuncs.com";

    @Bean
    public OSS ossClient() {
        return new OSSClientBuilder().build(this.ossEndpoint, this.accessKey, this.accessSecret);
    }

    @Bean(name = "oss-acs-client")
    public DefaultAcsClient getAcsClient() {
        IClientProfile profile = DefaultProfile.getProfile("", this.accessKey, this.accessSecret);
        return new DefaultAcsClient(profile);
    }

}
