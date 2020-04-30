package com.keepreal.madagascar.common.snowflake.annotation;

import com.keepreal.madagascar.common.snowflake.config.IdGeneratorConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents the {@link EnableIdGenerator} annotaion.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(IdGeneratorConfiguration.class)
public @interface EnableIdGenerator {
}
