package com.keepreal.madagascar.lemur.config;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.profile.DefaultProfile;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@ConfigurationProperties(prefix = "aliyun-vod")
@Data
public class AcsClientConfig {

    private String regionId;
    private String accessKey;
    private String accessSecret;

    @Bean(name = "vod-acs-client")
    @Primary
    public DefaultAcsClient acsClient() {
        DefaultProfile profile = DefaultProfile.getProfile(regionId, this.accessKey, this.accessSecret);
        return new DefaultAcsClient(profile);
    }

}