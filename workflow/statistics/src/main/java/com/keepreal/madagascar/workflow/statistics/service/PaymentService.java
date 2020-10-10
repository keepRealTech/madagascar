package com.keepreal.madagascar.workflow.statistics.service;

import com.keepreal.madagascar.workflow.statistics.model.coua.IslandInfo;
import com.keepreal.madagascar.workflow.statistics.repository.vanga.PaymentRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
public class PaymentService {

    private final static String LARK_MESSAGE_TITLE = "昨日数据播报";

    private final PaymentRepository paymentRepository;
    private final IslandService islandService;
    private final LarkService larkService;

    public PaymentService(PaymentRepository paymentRepository,
                          IslandService islandService,
                          LarkService larkService) {
        this.paymentRepository = paymentRepository;
        this.islandService = islandService;
        this.larkService = larkService;
    }

    public void run() {
        long today = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long yesterday = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).minusDays(1L).toInstant().toEpochMilli();

        Integer userYesterday = this.paymentRepository.countUserYesterday(yesterday, today);
        Integer userTotal = this.paymentRepository.countUserTotal();

        List<String> creatorYesterdayIdList = this.paymentRepository.countCreatorYesterday(yesterday, today);
        Integer creatorTotal = this.paymentRepository.countCreatorTotal();

        List<IslandInfo> islandInfoList = this.islandService.retrieveIslandsByHostIds(creatorYesterdayIdList);

        StringBuilder sb = new StringBuilder();
        sb.append("【昨日付费用户数: ").append(userYesterday).append("】");
        sb.append("【累计付费用户数: ").append(userTotal).append("】");
        sb.append("【昨日有收入的创作者数量: ").append(creatorYesterdayIdList.size()).append("】");
        sb.append("【累计有收入的创作者数量: ").append(creatorTotal).append("】");

        islandInfoList.forEach(islandInfo -> {
            Integer userYesterdayByPayeeId = this.paymentRepository.countYesterdayByPayeeId(islandInfo.getHostId(), yesterday, today);
            Integer amountYesterdayByPayeeId = this.paymentRepository.countAmountYesterdayByPayeeId(islandInfo.getHostId(), yesterday, today);
            sb.append("【岛名: ")
                    .append(islandInfo.getIslandName())
                    .append(", 昨日支持人数: ")
                    .append(userYesterdayByPayeeId)
                    .append(", 昨日支持金额: ")
                    .append(BigDecimal.valueOf(Long.valueOf(amountYesterdayByPayeeId)).divide(new BigDecimal(100)).toString())
                    .append("】");
        });

        this.larkService.sendMessage(LARK_MESSAGE_TITLE, sb.toString());
    }
}
