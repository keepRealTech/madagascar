package com.keepreal.madagascar.workflow.reconciliation.service;

import com.keepreal.madagascar.workflow.reconciliation.wechatPay.WXPay;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class WechatBillService {

    private final WXPay client;

    public WechatBillService(WXPay client) {
        this.client = client;
    }

    public Map<String, String> downloadBill() throws Exception {
        Map<String, String> reqData = new HashMap<>();
        reqData.put("bill_date", LocalDate.now().plusDays(-1L).format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        reqData.put("bill_type", "SUCCESS");
        return client.downloadBill(reqData);
    }
}
