package com.keepreal.madagascar.common.stats_events.annotation;

import com.keepreal.madagascar.brookesia.StatsEventAction;
import com.keepreal.madagascar.brookesia.StatsEventCategory;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents the {@link HttpStatsEventTrigger} annotation.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HttpStatsEventTrigger {

    /**
     * Represents the event category.
     */
    StatsEventCategory category() default StatsEventCategory.STATS_CAT_NONE;

    /**
     * Represents the event action.
     */
    StatsEventAction action() default StatsEventAction.STATS_ACT_NONE;

    /**
     * Represents the event label string.
     */
    String label() default "";

    /**
     * Represents the value logic in SpEL. The evaluation context will be the response.
     */
    String value() default "body.rtn";

    /**
     * Represents the succeed value logic in SpEL. The evaluation context will be the response.
     */
    String succeed() default "getStatusCode().value() == 200";

    /**
     * Represents the metadata value logic in SpEL. The evaluation context will be the request args.
     */
    String metadata() default "";

}

