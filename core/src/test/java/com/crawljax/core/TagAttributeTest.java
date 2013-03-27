package com.crawljax.core;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TagAttributeTest {

	@Test
	public void matchesValueWildcard() {
		TagAttribute attrib = new TagAttribute("id", "some%");
		assertTrue(attrib.matchesValue("something"));
		assertFalse(attrib.matchesValue("sompthing"));
	}

	@Test
	public void matchesNameValue() {
		TagAttribute attrib = new TagAttribute("class", "hidden");
		assertTrue(attrib.matchesValue("hidden header"));
		assertFalse(attrib.matchesValue("hid"));
	}
}
