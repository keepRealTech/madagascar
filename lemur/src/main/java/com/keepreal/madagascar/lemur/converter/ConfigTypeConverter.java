package com.keepreal.madagascar.lemur.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import swagger.model.ConfigType;

@Component
public class ConfigTypeConverter implements Converter<String, ConfigType> {
    @Override
    public ConfigType convert(String source) {
        if ("null".equals(source)) {
            return null;
        }

        if ("android".equals(source.toLowerCase())) {
            return ConfigType.ANDROID;
        }

        if ("ios".equals(source.toLowerCase())) {
            return ConfigType.IOS;
        }

        return ConfigType.fromValue(source);
    }
}
