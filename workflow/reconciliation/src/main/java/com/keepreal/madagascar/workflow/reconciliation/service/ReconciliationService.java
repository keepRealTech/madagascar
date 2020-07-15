package com.keepreal.madagascar.workflow.reconciliation.service;

import com.keepreal.madagascar.common.workflow.model.WorkflowLog;
import com.keepreal.madagascar.common.workflow.service.WorkflowService;
import com.keepreal.madagascar.workflow.reconciliation.model.ReconciliationInfo;
import com.keepreal.madagascar.workflow.reconciliation.model.WeChatBill;
import com.keepreal.madagascar.workflow.reconciliation.model.WechatOrder;
import com.keepreal.madagascar.workflow.reconciliation.model.WechatOrderState;
import com.keepreal.madagascar.workflow.reconciliation.repository.WechatOrderRepository;
import com.keepreal.madagascar.workflow.reconciliation.util.BillConvert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Represents the reconciliation service.
 */
@Service
@Slf4j
public class ReconciliationService {
    private static final String ORDER_NOT_EXIST = "order-not-exist";
    private static final String AMOUNT_NOT_MATCH = "amount-not-match";
    private static final String STATE_NOT_MATCH = "state-not-exist";

    private final WechatBillService wechatBillService;
    private final WechatOrderRepository wechatOrderRepository;
    private final WorkflowService workflowService;

    /**
     * Constructs the reconciliation service.
     *
     * @param wechatBillService     {@link WechatBillService}.
     * @param wechatOrderRepository {@link WechatOrderRepository}.
     * @param workflowService       {@link WorkflowService}.
     */
    public ReconciliationService(WechatBillService wechatBillService,
                                 WechatOrderRepository wechatOrderRepository,
                                 WorkflowService workflowService) {
        this.wechatBillService = wechatBillService;
        this.wechatOrderRepository = wechatOrderRepository;
        this.workflowService = workflowService;
    }

    public void run() {
        log.info("Starting workflow [reconciliation].");
        Map<String, String> map;
        WorkflowLog workflowLog = this.workflowService.initialize();
        try {
            map = wechatBillService.downloadBill();
            if ("SUCCESS".equals(map.get("return_code"))) {
                String data = map.get("data");
                List<WeChatBill> weChatBillList = BillConvert.analyze(data, LocalDate.now().plusDays(-1L).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

                weChatBillList.forEach(bill -> {
                    WechatOrder wechatOrder = wechatOrderRepository.findTopByTradeNumberAndDeletedIsFalse(bill.getMchOrderNo());
                    String billStr = bill.getAmount().replaceAll("\\.", "");
                    if (wechatOrder == null) {
                        workflowLog.getReconciliationInfos().add(ReconciliationInfo.builder()
                                .tradeNumber(bill.getMchOrderNo())
                                .type(ORDER_NOT_EXIST)
                                .build().toString());
                        log.error("local wechat order not exist! trade number is {}", bill.getMchOrderNo());
                    } else if (!Long.valueOf(billStr).equals(Long.valueOf(wechatOrder.getFeeInCents()))) {
                        workflowLog.getReconciliationInfos().add(ReconciliationInfo.builder()
                                .tradeNumber(bill.getMchOrderNo())
                                .type(AMOUNT_NOT_MATCH)
                                .fullInformation("wechat_amount is " + billStr + "local fee in cents is " + wechatOrder.getFeeInCents())
                                .build().toString());
                        log.error("amount not match! wechat_amount is {}, local fee in cents is {}, trade number is {}", billStr, wechatOrder.getFeeInCents(), bill.getMchOrderNo());
                    } else if (!wechatOrder.getState().equals(WechatOrderState.SUCCESS.getValue())) {
                        workflowLog.getReconciliationInfos().add(ReconciliationInfo.builder()
                                .tradeNumber(bill.getMchOrderNo())
                                .type(STATE_NOT_MATCH)
                                .fullInformation("local state is " + wechatOrder.getState())
                                .build().toString());
                        log.error("state not match! local order state is {}, trade number is {}", wechatOrder.getState(), bill.getMchOrderNo());
                    }
                });
                this.workflowService.succeed(workflowLog);
            } else if ("No Bill Exist".equals(map.get("return_msg"))) {
                workflowLog.setDescription("No bills available.");
                this.workflowService.succeed(workflowLog);
            } else {
                this.workflowService.failed(workflowLog, map.get("return_msg"));
                log.error("download bill fail! cause {}", map.get("return_msg"));
            }
        } catch (Exception e) {
            this.workflowService.failed(workflowLog, e);
        }
    }
}
