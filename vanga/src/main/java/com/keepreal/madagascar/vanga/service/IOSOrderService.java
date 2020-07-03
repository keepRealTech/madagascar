package com.keepreal.madagascar.vanga.service;

import com.aliyun.openservices.shade.com.alibaba.fastjson.JSONObject;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.vanga.config.IOSPayConfiguration;
import com.keepreal.madagascar.vanga.model.ShellSku;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Represents the ios order service.
 */
@Service
public class IOSOrderService {

    private final RestTemplate restTemplate;
    private final IOSPayConfiguration iosPayConfiguration;

    /**
     * Constructs the ios order service.
     *
     * @param restTemplate        {@link RestTemplate}.
     * @param iosPayConfiguration {@link IOSPayConfiguration}.
     */
    public IOSOrderService(RestTemplate restTemplate,
                           IOSPayConfiguration iosPayConfiguration) {
        this.restTemplate = restTemplate;
        this.iosPayConfiguration = iosPayConfiguration;
    }

    /**
     * Verifies the receipt is valid.
     *
     * @param receipt Receipt content.
     * @param sku     {@link ShellSku}.
     * @return Transaction id.
     */
    public String verify(String receipt, ShellSku sku) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        JSONObject requestBody = new JSONObject();
        requestBody.put("receipt-data", receipt);
        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);

        ResponseEntity<String> response = this.restTemplate.postForEntity(this.iosPayConfiguration.getVerifyUrl(),
                request, String.class);

        if (response.getStatusCode().isError()) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_GRPC_IOS_RECEIPT_VERIFY_ERROR, response.getStatusCode().name());
        }

        JSONObject responseData = JSONObject.parseObject(response.getBody());
        String status = responseData.getString("status");

        if (status.equals("21007")) {
            response = this.restTemplate.postForEntity(this.iosPayConfiguration.getVerifyUrlSandbox(),
                    request, String.class);
            if (response.getStatusCode().isError()) {
                throw new KeepRealBusinessException(ErrorCode.REQUEST_GRPC_IOS_RECEIPT_VERIFY_ERROR, response.getStatusCode().name());
            }
            responseData = JSONObject.parseObject(response.getBody());
            status = responseData.getString("status");
        }

        if (!status.equals("0")) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_GRPC_IOS_RECEIPT_VERIFY_ERROR, status);
        }

        List<HashMap> inApps = JSONObject.parseArray(
                JSONObject.parseObject(responseData.getString("receipt")).getString("in_app"), HashMap.class);

        Map<String, HashMap> dictionary = inApps.stream()
                .collect(Collectors.toMap(app -> app.get("product_id").toString(), Function.identity()));

        if (dictionary.containsKey(sku.getAppleSkuId())) {
            return dictionary.get(sku.getAppleSkuId()).get("transaction_id").toString();
        }

        throw new KeepRealBusinessException(ErrorCode.REQUEST_USER_SHELL_IOS_RECEIPT_INVALID_ERROR);
    }

}
