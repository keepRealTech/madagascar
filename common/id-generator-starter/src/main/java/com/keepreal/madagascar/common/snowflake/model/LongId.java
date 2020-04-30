package com.keepreal.madagascar.common.snowflake.model;

/**
 * Represents the snowflake id interface.
 */
public interface LongId {

    /**
     * Converts the snowflake id into long for storage.
     *
     * @return Long typed id.
     */
    long toLong();

}
