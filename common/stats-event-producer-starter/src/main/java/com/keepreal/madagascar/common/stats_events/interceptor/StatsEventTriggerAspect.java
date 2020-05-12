package com.keepreal.madagascar.common.stats_events.interceptor;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.bean.ProducerBean;
import com.keepreal.madagascar.brookesia.StatsEventMessage;
import com.keepreal.madagascar.common.stats_events.annotation.StatsEventTrigger;
import com.keepreal.madagascar.common.stats_events.messageFactory.MessageFactory;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;

/**
 * Represents the first version of stats event trigger aspect.
 */
@Aspect
@Component
@Slf4j
public class StatsEventTriggerAspect {

    private final MessageFactory messageFactory;
    private final ProducerBean producerBean;

    /**
     * Constructs the aspect.
     *
     * @param messageFactory {@link MessageFactory}.
     * @param producerBean   {@link ProducerBean}.
     */
    public StatsEventTriggerAspect(MessageFactory messageFactory,
                                   @Qualifier("stats-event-producer") ProducerBean producerBean) {
        this.messageFactory = messageFactory;
        this.producerBean = producerBean;
    }

    /**
     * Represents the annotation logic for sending event.
     *
     * @param joinPoint {@link ProceedingJoinPoint}.
     * @return The result for the method.
     * @throws Throwable {@link Throwable}.
     */
    @Around("@annotation(com.keepreal.madagascar.common.stats_events.annotation.StatsEventTrigger)")
    public Object statsEventTriggerAspectLogic(ProceedingJoinPoint joinPoint) throws Throwable {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        StatsEventTrigger annotation = method.getAnnotation(StatsEventTrigger.class);

        String value = "";
        String metadata = "";
        boolean succeed = false;

        StatsEventMessage.Builder statsEventMessageBuilder = StatsEventMessage.newBuilder()
                .setAction(annotation.action())
                .setCategory(annotation.category())
                .setLabel(annotation.label());

        if (StringUtils.hasText(annotation.metadata())) {
            ExpressionParser parser = new SpelExpressionParser();
            StandardEvaluationContext context = new StandardEvaluationContext(joinPoint.getArgs());
            try {
                metadata = String.valueOf(parser.parseExpression(annotation.metadata()).getValue(context));
            } catch (SpelEvaluationException ignored) {
            }
        }

        try {
            Object result = joinPoint.proceed();

            if (StringUtils.hasText(annotation.value())) {
                ExpressionParser parser = new SpelExpressionParser();
                StandardEvaluationContext context = new StandardEvaluationContext(result);
                try {
                    value = String.valueOf(parser.parseExpression(annotation.value()).getValue(context));
                } catch (SpelEvaluationException ignored) {
                }
            }

            if (StringUtils.hasText(annotation.succeed())) {
                ExpressionParser parser = new SpelExpressionParser();
                StandardEvaluationContext context = new StandardEvaluationContext(result);
                try {
                    succeed = Boolean.parseBoolean(String.valueOf(parser.parseExpression(annotation.succeed()).getValue(context)));
                } catch (SpelEvaluationException ignored) {
                }
            }

            return result;
        } catch (Exception exception) {
            value = exception.getMessage();
            throw exception;
        } finally {
            statsEventMessageBuilder
                    .setMetadata(metadata)
                    .setValue(value)
                    .setSucceed(succeed);
            Message message = this.messageFactory.valueOf(statsEventMessageBuilder);
            this.producerBean.sendAsync(message, null);
        }
    }

}
