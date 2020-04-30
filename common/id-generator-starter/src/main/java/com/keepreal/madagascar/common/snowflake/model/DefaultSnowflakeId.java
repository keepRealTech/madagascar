package com.keepreal.madagascar.common.snowflake.model;

import com.keepreal.madagascar.common.snowflake.config.IdGeneratorConfiguration;

/**
 * Represents a default snowflake id implementation.
 */
public class DefaultSnowflakeId implements LongId {

    private int nodeId;
    private long timestamp;
    private int sequenceId;

    /**
     * Constructs the default snowflake id.
     *
     * @param nodeId Node id.
     * @param timestamp Timestamp.
     * @param sequenceId Sequence id.
     */
    public DefaultSnowflakeId(int nodeId, long timestamp, int sequenceId) {
        this.nodeId = nodeId;
        this.timestamp = timestamp;
        this.sequenceId = sequenceId;
    }

    /**
     * Converts the snowflake id into long for storage.
     *
     * @return Long typed id.
     */
    @Override
    public long toLong() {
        return (this.timestamp << IdGeneratorConfiguration.NODE_ID_BITS + IdGeneratorConfiguration.SEQUENCE_BITS)
                | (this.nodeId << IdGeneratorConfiguration.SEQUENCE_BITS)
                | this.sequenceId;
    }

}