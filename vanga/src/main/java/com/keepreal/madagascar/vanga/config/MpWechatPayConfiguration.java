package com.keepreal.madagascar.vanga.config;

import com.keepreal.madagascar.vanga.wechatPay.IWXPayDomain;
import com.keepreal.madagascar.vanga.wechatPay.WXPay;
import com.keepreal.madagascar.vanga.wechatPay.WXPayConfig;
import com.keepreal.madagascar.vanga.wechatPay.WXPayConstants;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * Represents the configurations for wechat pay.
 */
@Configuration
@ConfigurationProperties(prefix = "wechat-mp", ignoreUnknownFields = false)
@Data
@EqualsAndHashCode(callSuper = true)
@Slf4j
public class MpWechatPayConfiguration extends WXPayConfig {

    private String appId;
    private String merchantId;
    private String merchantKey;
    private String callbackAddress;
    private String certPath;
    private String hostIp;

    /**
     * Implements the app id getter.
     *
     * @return App id.
     */
    public String getAppId() {
        return this.appId;
    }

    /**
     * Implements the merchant id getter.
     *
     * @return merchant id.
     */
    public String getMchId() {
        return this.merchantId;
    }

    /**
     * Implements the app key getter.
     *
     * @return App key.
     */
    public String getKey() {
        return this.merchantKey;
    }

    /**
     * Implements the merchant cert loader.
     *
     * @return Merchant cert input stream.
     */
    public InputStream getCertStream() {
        try {
            return Files.newInputStream(Paths.get(this.certPath), StandardOpenOption.READ);
        } catch (IOException e) {
            log.error("Could not locate wechat pay cert file.");
            return null;
        }
    }

    /**
     * Implements the wechat pay domain getter.
     *
     * @return {@link IWXPayDomain}.
     */
    public IWXPayDomain getWXPayDomain() {
        return new WechatPayDomain();
    }

    /**
     * Represents a wechat pay domain entity.
     */
    static class WechatPayDomain implements IWXPayDomain {
        @Override
        public void report(String domain, long elapsedTimeMillis, Exception ex) {
        }

        @Override
        public DomainInfo getDomain(WXPayConfig config) {
            return new DomainInfo(WXPayConstants.DOMAIN_API, true);
        }
    }

    /**
     * Represents the wechat pay client.
     *
     * @return {@link WXPay}.
     */
    @Bean(name = "mpwechatpay")
    @SneakyThrows
    public WXPay getWechatPayClient() {
        return new WXPay(this, this.callbackAddress, false);
    }

}
