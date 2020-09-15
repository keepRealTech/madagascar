package com.keepreal.madagascar.workflow.updatePayment.service;

import com.alibaba.fastjson.JSONObject;
import com.keepreal.madagascar.workflow.updatePayment.config.LoginConfiguration;
import org.apache.commons.codec.Charsets;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
public class LoginService {

    private final CloseableHttpClient httpClient;
    private final String username;
    private final String password;
    private final String uri;

    public LoginService(CloseableHttpClient httpClient,
                        LoginConfiguration loginConfiguration) {
        this.httpClient = httpClient;
        this.username = loginConfiguration.getUsername();
        this.password = loginConfiguration.getPassword();
        this.uri = loginConfiguration.getHost() + loginConfiguration.getPath();
    }

    public String getLoginToken() {
        CloseableHttpResponse response = null;
        try {
            HttpPost httpPost = new HttpPost(this.uri);

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("loginType", "LOGIN_PASSWORD");
            JSONObject data = new JSONObject();
            data.put("username", this.username);
            data.put("password", this.password);
            jsonObject.put("data", data);

            HttpEntity httpEntity = new StringEntity(jsonObject.toJSONString(), Charsets.UTF_8);

            httpPost.setEntity(httpEntity);
            httpPost.addHeader("Content-Type", "application/json");

            response = httpClient.execute(httpPost);

            if (200 == response.getStatusLine().getStatusCode()) {
                String strResult = EntityUtils.toString(response.getEntity());
                JSONObject loginResponse = JSONObject.parseObject(strResult);
                Map<String, String> loginData = (Map<String, String>) loginResponse.get("data");
                return loginData.get("token");
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
        return "";
    }
}
