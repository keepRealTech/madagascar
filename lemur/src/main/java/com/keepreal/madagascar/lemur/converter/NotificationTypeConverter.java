package com.keepreal.madagascar.lemur.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import swagger.model.NotificationType;

/**
 * Represents a converter for {@link NotificationType}.
 * This is here because of the lack support of swagger code generator.
 */
@Component
public class NotificationTypeConverter implements Converter<String, NotificationType> {

    /**
     * Converts the string into {@link NotificationType}.
     *
     * @param payload Payload in string.
     * @return {@link NotificationType}.
     */
    @Override
    public NotificationType convert(String payload) {
        if ("null".equals(payload)) {
            return null;
        }

        return NotificationType.fromValue(payload);
    }

}
