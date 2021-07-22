package com.crawljax.plugins.testcasegenerator.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

public class GsonUtils {
	public final static class ImmutableMapDeserializer
	        implements JsonDeserializer<ImmutableMap<?, ?>> {
		@Override
		public ImmutableMap<?, ?> deserialize(final JsonElement json, final Type type,
		        final JsonDeserializationContext context) throws JsonParseException {
			final Type type2 =
			        ParameterizedTypeImpl.make(Map.class,
			                ((ParameterizedType) type).getActualTypeArguments(), null);
			final Map<?, ?> map = context.deserialize(json, type2);
			return ImmutableMap.copyOf(map);
		}
	}
	
	public final static class ImmutableListDeserializer
    		implements JsonDeserializer<ImmutableList<?>> {
		@Override
		public ImmutableList<?> deserialize(final JsonElement json, final Type type,
		        final JsonDeserializationContext context) throws JsonParseException {
			final Type type2 =
			        ParameterizedTypeImpl.make(List.class,
			                ((ParameterizedType) type).getActualTypeArguments(), null);
			final List<?> list = context.deserialize(json, type2);
			return ImmutableList.copyOf(list);
}
}
}
