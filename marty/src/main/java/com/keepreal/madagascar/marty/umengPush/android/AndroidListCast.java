package com.keepreal.madagascar.marty.umengPush.android;

import com.aliyun.openservices.shade.com.alibaba.fastjson.JSONObject;
import com.keepreal.madagascar.marty.umengPush.UmengBaseMessageBody;

import java.util.HashMap;
import java.util.Map;

/**
 * Build android list cast message body
 */
public class AndroidListCast extends UmengBaseMessageBody {

    private Map<String, Object> payloadJsonObject = new HashMap<>();
    private Map<String, Object> bodyJsonObject = new HashMap<>();

    public AndroidListCast(String appkey) {
        super(appkey);
    }

    public void setDisplayType(String displayType) {
        this.payloadJsonObject.put("display_type", displayType);
    }

    public void setExtra(JSONObject extraJsonObject) {
        this.payloadJsonObject.put("extra", extraJsonObject.toJSONString());
    }

    public void setCustom(JSONObject jsonObject) {
        this.bodyJsonObject.put("custom", jsonObject);
    }

    public void setTicker(String ticker) {
        this.bodyJsonObject.put("ticker", ticker);
    }

    public void setTitle(String title) {
        this.bodyJsonObject.put("title", title);
    }

    public void setText(String text) {
        this.bodyJsonObject.put("text", text);
    }

    public void setAfterOpen(String afterOpen) {
        this.bodyJsonObject.put("after_open", afterOpen);
    }

    @Override
    public String toString() {
        this.payloadJsonObject.put("body", this.bodyJsonObject);
        setType("listcast");
        setPayload(this.payloadJsonObject);
        return super.toString();
    }
}
