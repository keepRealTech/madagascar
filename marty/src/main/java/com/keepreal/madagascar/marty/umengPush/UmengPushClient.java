package com.keepreal.madagascar.marty.umengPush;

import com.aliyun.openservices.shade.com.alibaba.fastjson.JSONObject;
import com.aliyun.openservices.shade.org.apache.commons.codec.digest.DigestUtils;
import com.keepreal.madagascar.marty.config.UmengConfiguration;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
public class UmengPushClient {

    private final String appMasterSecret;
    private final String url;
    private static final String HTTP_METHOD = "POST";
    private final RestTemplate restTemplate;

    public UmengPushClient(UmengConfiguration umengConfiguration,
                           RestTemplate restTemplate) {
        this.appMasterSecret = umengConfiguration.getAppMasterSecret();
        this.url = umengConfiguration.getUrl();
        this.restTemplate = restTemplate;
    }

    public void push(String pushMessage) {
        Map<String, String> urlVariables = new HashMap<>();
        urlVariables.put("sign", generatorSign(pushMessage));
        JSONObject responseJsonObject = restTemplate.postForObject(url, pushMessage, JSONObject.class, urlVariables);
    }

    private String generatorSign(String pushMessage) {
        return DigestUtils.md5Hex((HTTP_METHOD + url + pushMessage + appMasterSecret).getBytes(StandardCharsets.UTF_8));
    }
}
