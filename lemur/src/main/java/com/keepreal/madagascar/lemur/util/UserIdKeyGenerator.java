package com.keepreal.madagascar.lemur.util;

import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Represents the cacheable key generator by user id.
 */
@Component(value = "UserIdKeyGenerator")
public class UserIdKeyGenerator implements KeyGenerator {

    /**
     * Implements the generate key method.
     *
     * @param target Target.
     * @param method Method.
     * @param params Params.
     * @return String key.
     */
    @Override
    public Object generate(Object target, Method method, Object... params) {
        return target.getClass().getSimpleName() + "-" + method.getName() + "-" + HttpContextUtils.getUserIdFromContext();
    }

}