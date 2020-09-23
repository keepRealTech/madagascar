package com.keepreal.madagascar.workflow.update_payment.service;

import com.keepreal.madagascar.workflow.update_payment.config.OrderCheckConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class OrderService {

    private final CloseableHttpClient httpClient;

    private final String uri;

    public OrderService(CloseableHttpClient httpClient,
                        OrderCheckConfiguration orderCheckConfiguration) {
        this.httpClient = httpClient;
        this.uri = orderCheckConfiguration.getHost() + orderCheckConfiguration.getPathTemplate();
    }

    public void checkOrder(String orderId, String loginToken) {
        CloseableHttpResponse response = null;
        try {
            HttpPost httpPost = new HttpPost(String.format(this.uri, orderId));

            httpPost.addHeader("Content-Type", "application/json");
            httpPost.addHeader("Authorization", "bearer " + loginToken);

            response = httpClient.execute(httpPost);

            if (200 != response.getStatusLine().getStatusCode()) {
                log.error("check order error! orderId is {}, httpCode is {}", orderId, response.getStatusLine().getStatusCode());
            }
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
