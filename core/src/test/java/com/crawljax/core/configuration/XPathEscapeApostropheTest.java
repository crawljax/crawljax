package com.crawljax.core.configuration;

import com.crawljax.core.state.Eventable.EventType;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class XPathEscapeApostropheTest {

	private CrawlElement element;

	@Before
	public void setup() {
		element = new CrawlElement(EventType.click, "button");
	}

	@Test
	public void testStringNoApostrophes() {
		String test = "Test String";
		test = element.escapeApostrophes(test);
		assertEquals("'Test String'", test);
	}

	@Test
	public void testStringConcat() {
		String test = "I'm Feeling Lucky";
		test = element.escapeApostrophes(test);
		assertEquals("concat('I',\"'\",'m Feeling Lucky')", test);
	}

}
