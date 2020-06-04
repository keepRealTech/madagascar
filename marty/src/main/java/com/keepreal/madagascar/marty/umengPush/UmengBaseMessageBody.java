package com.keepreal.madagascar.marty.umengPush;

import com.aliyun.openservices.shade.com.alibaba.fastjson.JSONObject;

/**
 * Umeng base message body.
 */
public abstract class UmengBaseMessageBody {

    private JSONObject jsonObject = new JSONObject();

    public UmengBaseMessageBody(String appkey) {
        jsonObject.put("appkey", appkey);
        jsonObject.put("timestamp", String.valueOf(System.currentTimeMillis()));
    }

    public void setPayload(JSONObject payloadJsonObject) {
        jsonObject.put("payload", payloadJsonObject.toJSONString());
    }

    public void setDeviceToken(String tokens) {
        jsonObject.put("device_tokens", tokens);
    }

    public void setProductionMode(String productionMode) {
        jsonObject.put("production_mode", productionMode);
    }

    public void setType(String type) {
        jsonObject.put("type", type);
    }

    public void setPolicy(String policy) {
        jsonObject.put("policy", policy);
    }

    public void setDescription(String description) {
        jsonObject.put("description", description);
    }

    @Override
    public String toString() {
        return jsonObject.toJSONString();
    }
}
