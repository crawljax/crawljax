package com.crawljax.core;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TagAttributeTest {

	@Test
	public void matchesValue() {
		TagAttribute attrib = new TagAttribute("id", "some%");
		assertTrue(attrib.matchesValue("something"));
		assertFalse(attrib.matchesValue("sompthing"));

		attrib.setName("class");
		attrib.setValue("hidden");
		assertTrue(attrib.matchesValue("hidden header"));

		assertFalse(attrib.matchesValue("hid"));
	}
}
