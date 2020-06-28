package com.keepreal.madagascar.workflow.reconciliation.util;

import com.keepreal.madagascar.workflow.reconciliation.model.WeChatBill;

import java.util.ArrayList;
import java.util.List;

public class BillConvert {

    public static List<WeChatBill> analyze(String result, String date) {
        List<WeChatBill> bills = new ArrayList<WeChatBill>();
        String tradeMsg = result.substring(result.indexOf("`")); //去标题
        String tradeInfo = tradeMsg.substring(0, tradeMsg.indexOf("总")).replaceFirst(date, "").replaceAll("`", "");
        ;// 去掉汇总数据
        String[] tradeArray = tradeInfo.split(date); //通过交易时间分隔   订单数
        for (String trade : tradeArray) {
            String[] order = trade.split(",");
            WeChatBill bill = new WeChatBill();
            bill.setDate(date + order[0]);
            bill.setAppId(order[1]);
            bill.setMch_id(order[2]);
            bill.setSubMch_id(order[3]);
            bill.setDevice_info(order[4]);
            bill.setWeiXinOrderNo(order[5]);
            bill.setMchOrderNo(order[6]);
            bill.setUserId(order[7]);
            bill.setType(order[8]);
            bill.setStatus(order[9]);
            bill.setBank(order[10]);
            bill.setCurrency(order[11]);
            bill.setAmount(order[12]);
            bill.setEnvelopeAmount(order[13]);
            bill.setName(order[14]);
            bill.setPacket(order[15]);
            bill.setPoundage(order[16]);
            bill.setRate(order[17]);
            bill.setOrderAmount(order[18]);
            bills.add(bill);
        }
        return bills;
    }
}
