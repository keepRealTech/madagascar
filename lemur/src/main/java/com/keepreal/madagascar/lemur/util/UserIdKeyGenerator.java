package com.keepreal.madagascar.lemur.util;

import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Component(value = "UserIdKeyGenerator")
public class UserIdKeyGenerator implements KeyGenerator {

    @Override
    public Object generate(Object target, Method method, Object... params) {
        return target.getClass().getSimpleName() + "-" + method.getName() + "-" + HttpContextUtils.getUserIdFromContext();
    }

}