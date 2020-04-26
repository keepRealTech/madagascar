package com.keepreal.madagascar.lemur.converter;

import com.google.gson.Gson;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import swagger.model.PutUserPayload;

/**
 * Represents a converter for {@link PutUserPayload}.
 * This is here because of the lack support of swagger code generator.
 */
@Component
public class PutUserPayloadConverter implements Converter<String, PutUserPayload> {

    private final Gson gson;

    /**
     * Constructs the converter.
     */
    public PutUserPayloadConverter() {
        this.gson = new Gson();
    }

    /**
     * Converts the string into entity.
     *
     * @param payload Payload in json.
     * @return {@link PutUserPayload}.
     */
    @Override
    public PutUserPayload convert(String payload) {
        return this.gson.fromJson(payload, PutUserPayload.class);
    }
}
