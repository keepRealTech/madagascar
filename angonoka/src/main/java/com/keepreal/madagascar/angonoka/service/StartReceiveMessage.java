package com.keepreal.madagascar.angonoka.service;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 建立长连接 获取微博订阅数据
 */
@Component
public class StartReceiveMessage implements CommandLineRunner {

    private final ReceiveMessageService receiveMessageService;

    public StartReceiveMessage(ReceiveMessageService receiveMessageService) {
        this.receiveMessageService = receiveMessageService;
    }

    @Override
    public void run(String... args) throws Exception {
        this.receiveMessageService.init();
    }
}
