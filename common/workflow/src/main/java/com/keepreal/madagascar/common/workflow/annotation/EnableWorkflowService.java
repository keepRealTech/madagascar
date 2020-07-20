package com.keepreal.madagascar.common.workflow.annotation;

import com.keepreal.madagascar.common.workflow.config.WorkflowConfiguration;
import com.keepreal.madagascar.common.workflow.repository.WorkflowRepository;
import com.keepreal.madagascar.common.workflow.service.WorkflowService;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents the {@link EnableWorkflowService} annotation.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({WorkflowService.class, WorkflowConfiguration.class, WorkflowRepository.class})
public @interface EnableWorkflowService {
}
