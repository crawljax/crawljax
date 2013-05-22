package com.crawljax.plugins.crawloverview.model;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.slf4j.LoggerFactory;

import com.crawljax.core.plugin.Plugin;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.guava.GuavaModule;

public class Serializer {

	private static final ObjectMapper MAPPER;

	static {
		MAPPER = new ObjectMapper();
		MAPPER.getSerializationConfig().getDefaultVisibilityChecker()
		        .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
		        .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
		        .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
		        .withCreatorVisibility(JsonAutoDetect.Visibility.NONE);
		MAPPER.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

		MAPPER.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.getDefault()));

		MAPPER.registerModule(new GuavaModule());
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
	public static String toPrettyJson(Object o) {
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

	public static <T> T deserialize(String value, TypeReference<T> clasz) throws IOException {
		return MAPPER.readValue(value, clasz);
	}

	private Serializer() {
	}

	public static OutPutModel read(String json) throws JsonParseException, JsonMappingException,
	        IOException {
		return MAPPER.readValue(json, OutPutModel.class);
	}

	public static OutPutModel read(File file) throws IOException {
		return MAPPER.readValue(file, OutPutModel.class);
	}
}
