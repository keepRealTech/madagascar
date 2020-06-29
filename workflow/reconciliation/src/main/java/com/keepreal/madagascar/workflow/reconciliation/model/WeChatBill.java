package com.keepreal.madagascar.workflow.reconciliation.model;

import lombok.Data;

@Data
public class WeChatBill {

    private String date;//﻿交易时间
    private String appId;//公众账号ID
    private String mch_id;//商户号
    private String subMch_id;//子商户号 特约商户号
    private String device_info;//设备号
    private String weiXinOrderNo;//微信订单号
    private String mchOrderNo;//商户订单号
    private String userId;//用户标识
    private String type;//交易类型
    private String status;//交易状态
    private String bank;//付款银行
    private String currency;//货币种类
    private String amount;//总金额
    private String envelopeAmount;//企业红包金额 代金券金额
    private String name;//商品名称
    private String packet;//商户数据包
    private String poundage;//手续费
    private String rate;//费率
    private String orderAmount; //订单金额

}
