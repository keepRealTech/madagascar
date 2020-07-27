package com.keepreal.madagascar.marty.umengPush;

import com.aliyun.openservices.shade.com.alibaba.fastjson.JSONObject;
import com.aliyun.openservices.shade.org.apache.commons.codec.digest.DigestUtils;
import com.keepreal.madagascar.marty.config.UmengConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;

/**
 * Represent the umeng push client.
 */
@Component
@Slf4j
public class UmengPushClient {

    private final String appMasterSecret;
    private final String url;
    private static final String HTTP_METHOD = "POST";
    private final RestTemplate restTemplate;

    /**
     * Constructs the umeng push client.
     *
     * @param umengConfiguration    {@link UmengConfiguration}.
     * @param restTemplate          {@link RestTemplate}.
     */
    public UmengPushClient(UmengConfiguration umengConfiguration,
                           RestTemplate restTemplate) {
        this.appMasterSecret = umengConfiguration.getAndroidAppMasterSecret();
        this.url = umengConfiguration.getUrl();
        this.restTemplate = restTemplate;
    }

    /**
     * push by umeng.
     *
     * @param pushMessage   push message.
     */
    public void push(String pushMessage) {
        try {
            String sign = "?sign="+generatorSign(pushMessage);
            restTemplate.postForObject(url + sign, pushMessage, JSONObject.class);
        } catch (RestClientException e) {
            log.error("exception: {}", e);
        }
    }

    /**
     * generator sign by umeng regulation.
     *
     * @param pushMessage   push message.
     * @return  generated sign.
     */
    private String generatorSign(String pushMessage) {
        return DigestUtils.md5Hex((HTTP_METHOD + url + pushMessage + appMasterSecret).getBytes(StandardCharsets.UTF_8));
    }
}
