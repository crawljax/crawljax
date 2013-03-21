package com.crawljax.plugins.crawloverview;

import java.io.IOException;
import java.text.SimpleDateFormat;

import org.slf4j.LoggerFactory;

import com.crawljax.core.plugin.Plugin;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class Serializer {

	private static final ObjectMapper MAPPER;

	static {
		MAPPER = new ObjectMapper();
		MAPPER.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
		MAPPER.setVisibility(PropertyAccessor.GETTER, Visibility.NONE);
		MAPPER.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
		MAPPER.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:00:ss z"));
		SimpleModule testModule = new SimpleModule("Plugin serialiezr");
		testModule.addSerializer(new JsonSerializer<Plugin>() {

			@Override
			public void serialize(Plugin plugin, JsonGenerator jgen,
			        SerializerProvider provider) throws IOException, JsonProcessingException {
				jgen.writeString(plugin.getClass().getSimpleName());
			}

			@Override
			public Class<Plugin> handledType() {
				return Plugin.class;
			}
		});
		MAPPER.registerModule(testModule);
	}

	/**
	 * Serialize the object JSON. When an error occures return a string with the given error.
	 */
	static String toPrettyJson(Object o) {
		try {
			return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(o);
		} catch (JsonProcessingException e) {
			LoggerFactory
			        .getLogger(Serializer.class)
			        .error("Could not serialize the object. This will be ignored and the error will be written instead. Object was {}",
			                o, e);
			return "\"" + e.getMessage() + "\"";
		}
	}
}
