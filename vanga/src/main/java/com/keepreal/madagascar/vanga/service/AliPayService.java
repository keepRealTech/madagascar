package com.keepreal.madagascar.vanga.service;

import com.alipay.easysdk.factory.Factory;
import com.alipay.easysdk.factory.Factory.Payment;
import com.alipay.easysdk.kernel.Config;
import com.alipay.easysdk.payment.app.models.AlipayTradeAppPayResponse;
import com.alipay.easysdk.payment.common.models.AlipayTradeCreateResponse;
import com.keepreal.madagascar.vanga.config.AlipayConfiguration;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.UUID;

@Service
public class AliPayService {

    private final AlipayConfiguration alipayConfiguration;

    public AliPayService(AlipayConfiguration alipayConfiguration) {
        this.alipayConfiguration = alipayConfiguration;
        Config config = new Config();
        config.protocol = "https";
        config.gatewayHost = "openapi.alipay.com";
        config.signType = "RSA2";

        config.appId = this.alipayConfiguration.getAppId();
        config.merchantPrivateKey = this.alipayConfiguration.getMerchantKey();
        config.notifyUrl = this.alipayConfiguration.getCallbackAddress();

        config.merchantCertPath = this.alipayConfiguration.getMerchantCertPath();
        config.alipayCertPath = this.alipayConfiguration.getAlipayCertPath();
        config.alipayRootCertPath = this.alipayConfiguration.getAlipayRootPath();

        Factory.setOptions(config);
    }

    @SneakyThrows
    public void tryPlaceOrderApp(String feeInCents, String description) {
        String tradeNum = UUID.randomUUID().toString().replace("-", "");

        AlipayTradeAppPayResponse response = Payment.App().pay(description, tradeNum, feeInCents);
        String s = response.body;
    }

    @PostConstruct
    private void init() {
        this.tryPlaceOrderApp("1.00", "测试一下");
    }
}
