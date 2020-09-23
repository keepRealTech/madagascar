package com.keepreal.madagascar.workflow.update_payment.service;

import com.keepreal.madagascar.workflow.update_payment.repository.WechatOrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UpdatePaymentService {

    private final WechatOrderRepository wechatOrderRepository;
    private final LoginService loginService;
    private final OrderService orderService;
    private final LarkService larkService;
    private final long timeSpan = 15 * 60_000L; // 15minute

    public UpdatePaymentService(WechatOrderRepository wechatOrderRepository,
                                LoginService loginService,
                                OrderService orderService,
                                LarkService larkService) {
        this.wechatOrderRepository = wechatOrderRepository;
        this.loginService = loginService;
        this.orderService = orderService;
        this.larkService = larkService;
    }

    public void process() {
        String loginToken = this.loginService.getLoginToken();

        List<String> wechatOrderIds = wechatOrderRepository.findIdByState(System.currentTimeMillis() - timeSpan);

        if (wechatOrderIds.size() > 50) {
            this.larkService.sendToLark(wechatOrderIds.size());
        } else {
            wechatOrderIds.forEach(id -> this.orderService.checkOrder(id, loginToken));
        }
    }
}
