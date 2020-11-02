package com.keepreal.madagascar.lemur.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import swagger.model.MultiMediaType;

/**
 * Represents a converter for {@link MultiMediaType}.
 * This is here because of the lack support of swagger code generator.
 */
@Component
public class MultiMediaTypeConverter implements Converter<String, MultiMediaType> {

    /**
     * Converts the string into {@link MultiMediaType}.
     *
     * @param payload Payload in string.
     * @return {@link MultiMediaType}.
     */
    @Override
    public MultiMediaType convert(String payload) {
        if ("null".equals(payload)) {
            return null;
        }

        return MultiMediaType.fromValue(payload);
    }

}
