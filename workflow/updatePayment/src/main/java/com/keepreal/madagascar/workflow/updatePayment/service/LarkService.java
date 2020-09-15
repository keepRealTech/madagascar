package com.keepreal.madagascar.workflow.updatePayment.service;

import com.keepreal.madagascar.workflow.updatePayment.config.LarkConfiguration;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class LarkService {

    private final LarkConfiguration larkConfiguration;
    private final CloseableHttpClient httpClient;

    public LarkService(LarkConfiguration larkConfiguration,
                       CloseableHttpClient httpClient) {
        this.larkConfiguration = larkConfiguration;
        this.httpClient = httpClient;
    }

    public void sendToLark(int orderCount) {
        CloseableHttpResponse response = null;
        try {
            HttpPost httpPost = new HttpPost(this.larkConfiguration.getWebhook());
            httpPost.addHeader("Content-Type", "application/json");
            String requestBody = String.format("{\"title\":\"微信支付掉单报警\", \"text\":\"最近一次有%d单状态异常\"}", orderCount);
            HttpEntity httpEntity = new StringEntity(requestBody);
            httpPost.setEntity(httpEntity);

            response = httpClient.execute(httpPost);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (response != null) {
                try {
                    EntityUtils.consume(response.getEntity());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
