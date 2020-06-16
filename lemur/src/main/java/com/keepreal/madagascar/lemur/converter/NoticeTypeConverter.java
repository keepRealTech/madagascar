package com.keepreal.madagascar.lemur.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import swagger.model.NoticeType;

/**
 * Represents a converter for {@link NoticeType}.
 * This is here because of the lack support of swagger code generator.
 */
@Component
public class NoticeTypeConverter implements Converter<String, NoticeType> {

    /**
     * Converts the string into {@link NoticeType}.
     *
     * @param payload Payload in string.
     * @return {@link NoticeType}.
     */
    @Override
    public NoticeType convert(String payload) {
        if ("null".equals(payload)) {
            return null;
        }

        return NoticeType.fromValue(payload);
    }

}
