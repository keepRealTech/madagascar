package com.keepreal.madagascar.fossa.config;

import com.aliyun.openservices.ons.api.PropertyKeyConst;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-05-05
 **/

@Configuration
@ConfigurationProperties(prefix = "rocketmq")
@Data
public class NotificationEventProducerConfiguration {

    private String accessKey;
    private String secretKey;
    private String nameSrvAddr;
    private String topic;
    private String tag;

    public Properties getMqPropertie() {
        Properties properties = new Properties();
        properties.setProperty(PropertyKeyConst.AccessKey, this.accessKey);
        properties.setProperty(PropertyKeyConst.SecretKey, this.secretKey);
        properties.setProperty(PropertyKeyConst.NAMESRV_ADDR, this.nameSrvAddr);
        return properties;
    }
}
