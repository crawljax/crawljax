package com.crawljax.plugins.crawloverview;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableSetMultimap;

public class SerializerTest {

	@Test
	public void testGuavaObjectsSerialize() throws IOException {
		ImmutableSetMultimap<String, String> map =
		        ImmutableSetMultimap.of("a", "a1", "a", "a2", "b", "b1");
		String asJsonString = Serializer.toPrettyJson(map);

		TypeReference<ImmutableSetMultimap<String, String>> ref =
		        new TypeReference<ImmutableSetMultimap<String, String>>() {
		        };
		ImmutableSetMultimap<String, String> deserialized =
		        Serializer.deserialize(asJsonString, ref);

		assertThat(deserialized, is(map));
	}
}
