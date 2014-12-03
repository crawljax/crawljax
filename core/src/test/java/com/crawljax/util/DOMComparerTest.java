package com.crawljax.util;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.List;

import org.custommonkey.xmlunit.Difference;
import org.hamcrest.collection.IsEmptyCollection;
import org.junit.Test;
import org.w3c.dom.Document;

/**
 * Test the comparisons between two Documents.
 * 
 * @author Singla
 */

public class DOMComparerTest {

	@Test
	public void compareNoDifference() throws IOException {
		String html = "<html><body><p>No difference</p></body></html>";

		Document control = DomUtils.asDocument(html);
		assertNotNull(control);

		Document test = DomUtils.asDocument(html);
		assertNotNull(test);

		DOMComparer dc = new DOMComparer(control, test);

		List<Difference> differences = dc.compare();
		assertThat(differences, is(IsEmptyCollection.empty()));
	}

	@Test
	public void comparePartialDifference() throws IOException {
		String controlHTML =
		        "<html><body><header>Crawljax</header><p>There are differences</p></body></html>";
		String testHTML =
		        "<html><head><title>Crawljax</title></head><body><p>There are differences.</body></html>";
		final int EXPECTED_DIFF = 7;

		Document control = DomUtils.asDocument(controlHTML);
		assertNotNull(control);

		Document test = DomUtils.asDocument(testHTML);
		assertNotNull(test);

		DOMComparer dc = new DOMComparer(control, test);

		List<Difference> differences = dc.compare();
		assertEquals("Error: Did not find 5 differences", differences.size(), EXPECTED_DIFF);

	}

}