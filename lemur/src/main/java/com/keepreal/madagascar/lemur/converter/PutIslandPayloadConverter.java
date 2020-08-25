package com.keepreal.madagascar.lemur.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import lombok.SneakyThrows;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import swagger.model.PutIslandPayload;

/**
 * Represents a converter for {@link PutIslandPayload}.
 * This is here because of the lack support of swagger code generator.
 */
@Component
public class PutIslandPayloadConverter implements Converter<String, PutIslandPayload> {

    private final ObjectMapper objectMapper;

    /**
     * Constructs the converter.
     */
    public PutIslandPayloadConverter() {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Converts the string into entity.
     *
     * @param payload Payload in json.
     * @return {@link PutIslandPayload}.
     */
    @SneakyThrows
    @Override
    public PutIslandPayload convert(String payload) {
        return this.objectMapper.readValue(payload, PutIslandPayload.class);
    }

}
