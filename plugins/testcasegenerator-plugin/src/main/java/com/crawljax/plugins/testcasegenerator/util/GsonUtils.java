package com.crawljax.plugins.testcasegenerator.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.reflect.TypeUtils;

public class GsonUtils {

    public static final class ImmutableMapDeserializer implements JsonDeserializer<ImmutableMap<?, ?>> {

        @Override
        public ImmutableMap<?, ?> deserialize(
                final JsonElement json, final Type type, final JsonDeserializationContext context)
                throws JsonParseException {
            final Type type2 = TypeUtils.parameterize(ImmutableMap.class, type, null);
            // ParameterizedTypeImpl.make(Map.class, ((ParameterizedType) type).getActualTypeArguments(), null);
            final Map<?, ?> map = context.deserialize(json, type2);
            return ImmutableMap.copyOf(map);
        }
    }

    public static final class ImmutableListDeserializer implements JsonDeserializer<ImmutableList<?>> {

        @Override
        public ImmutableList<?> deserialize(
                final JsonElement json, final Type type, final JsonDeserializationContext context)
                throws JsonParseException {
            final Type type2 = TypeUtils.parameterize(ImmutableMap.class, type, null);
            // ParameterizedTypeImpl.make(List.class,       ((ParameterizedType) type).getActualTypeArguments(), null);
            final List<?> list = context.deserialize(json, type2);
            return ImmutableList.copyOf(list);
        }
    }
}
