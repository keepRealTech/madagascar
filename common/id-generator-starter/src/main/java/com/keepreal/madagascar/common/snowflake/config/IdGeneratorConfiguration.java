package com.keepreal.madagascar.common.snowflake.config;

import com.keepreal.madagascar.common.snowflake.generator.DefaultSnowflakeIdGenerator;
import com.keepreal.madagascar.common.snowflake.generator.LongIdGenerator;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 *  Represents the snowflake id configurations.
 */
@Configuration
@ConfigurationProperties(prefix = "snowflake", ignoreUnknownFields = false)
@Data
public class IdGeneratorConfiguration {

    public static final int TOTAL_BITS = 64;
    public static final int UNUSED_BITS = 1;
    public static final int EPOCH_BITS = 41;
    public static final int NODE_ID_BITS = 10;
    public static final int SEQUENCE_BITS = 12;

    public static final int MAX_NODE_ID = ~(-1 << IdGeneratorConfiguration.NODE_ID_BITS);
    public static final int MAX_SEQUENCE = ~(-1 << IdGeneratorConfiguration.SEQUENCE_BITS);

    private int nodeId;

    /**
     * Represents the bean of snowflake generator.
     *
     * @return Snowflake id generator.
     */
    @Bean
    public LongIdGenerator getGenerator() {
        return new DefaultSnowflakeIdGenerator(this);
    }

    /**
     * Does properties check.
     */
    @PostConstruct
    public void init() {
        if (this.nodeId < 0 || this.nodeId > IdGeneratorConfiguration.MAX_NODE_ID) {
            throw new IllegalArgumentException("Wrong node id.");
        }
    }

}