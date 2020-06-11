package com.keepreal.madagascar.marty.umengPush.ios;


import com.aliyun.openservices.shade.com.alibaba.fastjson.JSONObject;
import com.keepreal.madagascar.marty.umengPush.UmengBaseMessageBody;

public class IOSListCast extends UmengBaseMessageBody {

    private JSONObject payloadJsonObject = new JSONObject();
    private JSONObject apsJsonObject = new JSONObject();

    public IOSListCast(String appkey) {
        super(appkey);
    }
}
