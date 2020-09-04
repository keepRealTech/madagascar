package com.keepreal.madagascar.lemur.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import swagger.model.SceneType;

/**
 * Represents a converter for {@link SceneType}.
 * This is here because of the lack support of swagger code generator.
 */
@Component
public class SceneTypeConverter implements Converter<String, SceneType> {

    /**
     * Converts the string into {@link SceneType}.
     *
     * @param payload Payload in string.
     * @return {@link SceneType}.
     */
    @Override
    public SceneType convert(String payload) {
        if ("null".equals(payload)) {
            return null;
        }

        return SceneType.fromValue(payload);
    }

}