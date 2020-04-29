package com.keepreal.madagascar.lemur.converter;

import com.google.gson.Gson;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import swagger.model.PostFeedPayload;
import swagger.model.PutUserPayload;

/**
 * Represents a converter for {@link PostFeedPayload}.
 * This is here because of the lack support of swagger code generator.
 */
@Component
public class PostFeedPayloadConverter implements Converter<String, PostFeedPayload> {

    private final Gson gson;

    /**
     * Constructs the converter.
     */
    public PostFeedPayloadConverter() {
        this.gson = new Gson();
    }

    /**
     * Converts the string into entity.
     *
     * @param payload Payload in json.
     * @return {@link PutUserPayload}.
     */
    @Override
    public PostFeedPayload convert(String payload) {
        return this.gson.fromJson(payload, PostFeedPayload.class);
    }

}
