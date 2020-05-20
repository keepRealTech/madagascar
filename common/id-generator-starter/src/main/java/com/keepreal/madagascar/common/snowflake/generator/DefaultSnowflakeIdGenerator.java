package com.keepreal.madagascar.common.snowflake.generator;

import com.keepreal.madagascar.common.snowflake.config.IdGeneratorConfiguration;
import com.keepreal.madagascar.common.snowflake.model.DefaultSnowflakeId;

/**
 *  Represents a default snowflake id generator.
 */
public class DefaultSnowflakeIdGenerator implements LongIdGenerator{

    private volatile long lastTimestamp = -1L;
    private volatile int sequence = 0;
    private final IdGeneratorConfiguration configuration;

    /**
     * Constructs the default generator.
     *
     * @param configuration Configurations.
     */
    public DefaultSnowflakeIdGenerator(IdGeneratorConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * Generates the next snowflake id.
     *
     * @return Snowflake id.
     */
    @Override
    public synchronized long nextId() {
        long currentTimestamp = this.currentTimestamp();

        if (currentTimestamp < this.lastTimestamp) {
            throw new IllegalStateException("Invalid System Clock!");
        }

        if (this.lastTimestamp == currentTimestamp) {
            this.sequence = (this.sequence + 1) & IdGeneratorConfiguration.MAX_SEQUENCE;
            if (this.sequence == 0) {
                currentTimestamp = this.waitNextMillis(currentTimestamp);
            }
        } else {
            this.sequence = 0;
        }

        this.lastTimestamp = currentTimestamp;

        return new DefaultSnowflakeId(this.configuration.getNodeId(), this.lastTimestamp, this.sequence).toLong();
    }

    /**
     * Generates the current timestamp in milli.
     *
     * @return Timestamp in milli.
     */
    public long currentTimestamp() {
        return System.currentTimeMillis();
    }

    /**
     * Loops until next tick.
     *
     * @param currentTimestamp Current timestamp.
     * @return Updated timestamp.
     */
    private long waitNextMillis(long currentTimestamp) {
        while (currentTimestamp == this.lastTimestamp) {
            currentTimestamp = this.currentTimestamp();
        }
        return currentTimestamp;
    }

}
