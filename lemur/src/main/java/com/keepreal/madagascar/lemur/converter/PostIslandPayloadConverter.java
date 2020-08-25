package com.keepreal.madagascar.lemur.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import swagger.model.PostIslandPayload;
import swagger.model.PutUserPayload;

/**
 * Represents a converter for {@link PostIslandPayload}.
 * This is here because of the lack support of swagger code generator.
 */
@Component
public class PostIslandPayloadConverter implements Converter<String, PostIslandPayload> {

    private final ObjectMapper objectMapper;

    /**
     * Constructs the converter.
     */
    public PostIslandPayloadConverter() {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Converts the string into entity.
     *
     * @param payload Payload in json.
     * @return {@link PutUserPayload}.
     */
    @SneakyThrows
    @Override
    public PostIslandPayload convert(String payload) {
        return this.objectMapper.readValue(payload, PostIslandPayload.class);
    }

}
