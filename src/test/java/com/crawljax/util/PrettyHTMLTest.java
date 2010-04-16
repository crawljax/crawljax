package com.crawljax.util;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Test;

public class PrettyHTMLTest {

	private static final String TESTFILE = "src/test/java/com/crawljax/util/tuduDombefore.html";
	private static final String CONTROLFILE =
	        "src/test/java/com/crawljax/util/tuduDombefore.html.tidy";

	@Test
	public void prettifyHTML() {
		String testdom = Helper.getContent(new File(TESTFILE));
		String controldom = Helper.getContent(new File(CONTROLFILE));

		testdom = PrettyHTML.prettyHTML(testdom);

		assertEquals(controldom, testdom);
	}
}
