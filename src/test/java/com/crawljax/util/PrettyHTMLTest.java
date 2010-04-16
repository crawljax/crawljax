package com.crawljax.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;

import org.junit.Ignore;
import org.junit.Test;

public class PrettyHTMLTest {

	private static final String TESTFILE = "src/test/java/com/crawljax/util/tuduDombefore.html";
	private static final String CONTROLFILE =
	        "src/test/java/com/crawljax/util/tuduDombefore.html.tidy";

	@Ignore
	@Test
	public void prettifyHTML() {
		String testdom = Helper.getContent(new File(TESTFILE));
		String controldom = Helper.getContent(new File(CONTROLFILE));

		assertNotNull("File should be read", testdom);
		assertNotNull("File should be read", controldom);

		testdom = PrettyHTML.prettyHTML(testdom);

		assertEquals(controldom, testdom);
	}
}
