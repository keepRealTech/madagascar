package com.keepreal.madagascar.workflow.statistics.service;

import com.keepreal.madagascar.workflow.statistics.config.LarkConfiguration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Represents the lark message service.
 */
@Service
public class LarkService {

    private final LarkConfiguration larkConfiguration;
    private final RestTemplate restTemplate;

    /**
     * Constructs the lark service.
     *
     * @param larkConfiguration {@link LarkConfiguration}.
     * @param restTemplate      {@link RestTemplate}.
     */
    public LarkService(LarkConfiguration larkConfiguration,
                       RestTemplate restTemplate) {
        this.larkConfiguration = larkConfiguration;
        this.restTemplate = restTemplate;
    }

    /**
     * Sends the naive simple text to lark.
     *
     * @param title Title.
     * @param text  Text.
     */
    public void sendMessage(String title, String text) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        String requestBody = String.format("{\"title\":\"%s\", \"text\":\"%s\"}", title, text);
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
        this.restTemplate.exchange(this.larkConfiguration.getWebhook(), HttpMethod.POST, request, String.class);
    }

}
