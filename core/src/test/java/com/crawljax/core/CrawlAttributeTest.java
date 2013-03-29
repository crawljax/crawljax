package com.crawljax.core;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.crawljax.core.configuration.CrawlAttribute;

public class CrawlAttributeTest {

	@Test
	public void matchesValueWildcard() {
		CrawlAttribute attrib = new CrawlAttribute("id", "some%");
		assertTrue(attrib.matchesValue("something"));
		assertFalse(attrib.matchesValue("sompthing"));
	}

	@Test
	public void matchesNameValue() {
		CrawlAttribute attrib = new CrawlAttribute("class", "hidden");
		assertTrue(attrib.matchesValue("hidden header"));
		assertFalse(attrib.matchesValue("hid"));
	}
}
