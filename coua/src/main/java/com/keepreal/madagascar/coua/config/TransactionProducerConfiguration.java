package com.keepreal.madagascar.coua.config;

import com.aliyun.openservices.ons.api.PropertyKeyConst;
import com.aliyun.openservices.ons.api.bean.TransactionProducerBean;
import com.keepreal.madagascar.coua.service.CouaLocalTransactionCheckerService;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * Represents the merge user accounts transaction producer configurations.
 */
@Configuration
@ConfigurationProperties(prefix = "rocketmq.transaction")
@Data
public class TransactionProducerConfiguration {

    private final CouaLocalTransactionCheckerService couaLocalTransactionCheckerService;
    private String accessKey;
    private String secretKey;
    private String nameSrvAddr;
    private String topic;
    private String tag;

    public Properties getMqProperties() {
        Properties properties = new Properties();
        properties.setProperty(PropertyKeyConst.AccessKey, this.accessKey);
        properties.setProperty(PropertyKeyConst.SecretKey, this.secretKey);
        properties.setProperty(PropertyKeyConst.NAMESRV_ADDR, this.nameSrvAddr);
        return properties;
    }

    /**
     * Builds the transaction producer for merge user accounts.
     *
     * @return {@link TransactionProducerBean}.
     */
    @Bean(name = "transaction-event-producer", initMethod = "start", destroyMethod = "shutdown")
    public TransactionProducerBean buildProducer() {
        TransactionProducerBean transactionProducer = new TransactionProducerBean();

        transactionProducer.setProperties(this.getMqProperties());
        transactionProducer.setLocalTransactionChecker(this.couaLocalTransactionCheckerService);
        return transactionProducer;
    }

}
