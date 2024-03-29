package com.keepreal.madagascar.lemur.converter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.SneakyThrows;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import swagger.model.IslandAccessType;
import swagger.model.PutIslandPayload;

/**
 * Represents a converter for {@link PutIslandPayload}.
 * This is here because of the lack support of swagger code generator.
 */
@Component
public class PutIslandPayloadConverter implements Converter<String, PutIslandPayload> {

    private final Gson gson;

    /**
     * Constructs the converter.
     */
    public PutIslandPayloadConverter() {
        this.gson = new GsonBuilder()
                .registerTypeAdapter(IslandAccessType.class, new IslandAccessDeserializer())
                .create();
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
        return this.gson.fromJson(payload, PutIslandPayload.class);
    }

}
