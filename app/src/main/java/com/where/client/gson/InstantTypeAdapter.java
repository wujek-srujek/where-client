package com.where.client.gson;


import java.lang.reflect.Type;
import java.math.BigDecimal;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.threeten.bp.Instant;


public class InstantTypeAdapter implements JsonSerializer<Instant>, JsonDeserializer<Instant> {

    @Override
    public JsonElement serialize(Instant src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.getEpochSecond() + "." + src.getNano());
    }

    @Override
    public Instant deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        // algorithm:
        // original value may have nanos (i.e. have digits after decimal point)
        // 1. get the seconds count discarding the digits
        // 2. subtract it from the original value to get the fraction part
        // 3. move the comma by 9 places to the right (nano is 10^-9) to get the nanos count
        // 4. create the instance with seconds and nanos
        BigDecimal value = json.getAsBigDecimal();
        long seconds = value.longValue();
        long nanos = value.subtract(new BigDecimal(seconds)).movePointRight(9).longValue();

        return Instant.ofEpochSecond(seconds, nanos);
    }
}