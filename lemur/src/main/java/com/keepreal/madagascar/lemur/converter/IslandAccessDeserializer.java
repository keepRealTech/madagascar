package com.keepreal.madagascar.lemur.converter;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import swagger.model.IslandAccessType;

import java.lang.reflect.Type;
import java.util.Objects;

/**
 * Represents the type deserialization for {@link IslandAccessType}.
 */
public class IslandAccessDeserializer implements JsonDeserializer<IslandAccessType> {

    @Override
    public IslandAccessType deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (Objects.isNull(json) || "null".equals(json.getAsString())) {
            return null;
        }

        return IslandAccessType.fromValue(json.getAsString());
    }

}
