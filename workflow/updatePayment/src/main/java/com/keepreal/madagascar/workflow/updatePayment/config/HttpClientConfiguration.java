package com.keepreal.madagascar.workflow.updatePayment.config;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpClientConfiguration {

    @Bean
    public PoolingHttpClientConnectionManager poolingHttpClientConnectionManager() {
        PoolingHttpClientConnectionManager manager = new PoolingHttpClientConnectionManager();

        manager.setMaxTotal(500);
        manager.setDefaultMaxPerRoute(50);

        return manager;
    }

    @Bean
    public CloseableHttpClient closeableHttpClient(PoolingHttpClientConnectionManager poolingHttpClientConnectionManager) {
        return HttpClients.custom().setConnectionManager(poolingHttpClientConnectionManager).build();
    }
}
