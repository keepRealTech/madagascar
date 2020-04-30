package com.keepreal.madagascar.common.snowflake.generator;

/**
 *  Represents the snowflake generator interface.
 */
public interface LongIdGenerator {

    /**
     * Generates the next long id. Note: be careful about thread safety.
     *
     * @return Long id.
     */
    long nextId();

}
