package com.keepreal.madagascar.lemur.converter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.SneakyThrows;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import swagger.model.IslandAccessType;
import swagger.model.PostIslandPayloadV11;
import swagger.model.PutUserPayload;

/**
 * Represents a converter for {@link PostIslandPayloadV11}.
 * This is here because of the lack support of swagger code generator.
 */
@Component
public class PostIslandPayloadV11Converter implements Converter<String, PostIslandPayloadV11> {

    private final Gson gson;

    /**
     * Constructs the converter.
     */
    public PostIslandPayloadV11Converter() {
        this.gson = new GsonBuilder()
                .registerTypeAdapter(IslandAccessType.class, new IslandAccessDeserializer())
                .create();
    }

    /**
     * Converts the string into entity.
     *
     * @param payload Payload in json.
     * @return {@link PutUserPayload}.
     */
    @SneakyThrows
    @Override
    public PostIslandPayloadV11 convert(String payload) {
        return this.gson.fromJson(payload, PostIslandPayloadV11.class);
    }

}
