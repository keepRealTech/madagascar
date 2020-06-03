package com.keepreal.madagascar.marty.umengPush.android;

import com.aliyun.openservices.shade.com.alibaba.fastjson.JSONObject;
import com.keepreal.madagascar.marty.umengPush.UmengBaseMessageBody;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-06-03
 **/

public class AndroidListCast extends UmengBaseMessageBody {

    private JSONObject payloadJsonObject = new JSONObject();
    private JSONObject bodyJsonObject = new JSONObject();

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
        this.bodyJsonObject.put("custom", jsonObject.toJSONString());
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
        setPayload(this.payloadJsonObject);
        return super.toString();
    }
}
