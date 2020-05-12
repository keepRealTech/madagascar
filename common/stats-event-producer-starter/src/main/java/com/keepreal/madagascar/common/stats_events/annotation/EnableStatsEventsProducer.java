package com.keepreal.madagascar.common.stats_events.annotation;

import com.keepreal.madagascar.common.stats_events.config.StatsEventProducerConfiguration;
import com.keepreal.madagascar.common.stats_events.interceptor.StatsEventTriggerAspect;
import com.keepreal.madagascar.common.stats_events.messageFactory.MessageFactory;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents the {@link EnableStatsEventsProducer} annotation.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({StatsEventProducerConfiguration.class, StatsEventTriggerAspect.class, MessageFactory.class})
public @interface EnableStatsEventsProducer {
}
