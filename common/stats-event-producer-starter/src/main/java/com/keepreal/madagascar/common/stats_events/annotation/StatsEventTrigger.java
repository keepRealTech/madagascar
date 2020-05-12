package com.keepreal.madagascar.common.stats_events.annotation;

import com.keepreal.madagascar.brookesia.StatsEventAction;
import com.keepreal.madagascar.brookesia.StatsEventCategory;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents the {@link StatsEventTrigger} annotation.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface StatsEventTrigger {

    StatsEventCategory category() default StatsEventCategory.STATS_CAT_NONE;

    StatsEventAction action() default StatsEventAction.STATS_ACT_NONE;

    String label() default "";

    String value() default "";

    String succeed() default "getStatusCode().value() == 200";

    String metadata() default "";

}

