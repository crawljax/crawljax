package com.crawljax.util;

import static org.junit.Assert.*;

import java.io.IOException;

import java.util.List;
import org.junit.Test;
import org.custommonkey.xmlunit.Difference;
import org.w3c.dom.Document;

/**
 * Test the comparisons between two Documents.
 * @author Singla
 *
 */

public class DOMComparerTest   {
	
	private static int EMPTY = 0;
	
	@Test
	public void compareNoDifference() throws IOException{
		String html = "<html><body><p>No difference</p></body></html>";
	
		Document control = DomUtils.asDocument(html);
		assertNotNull(control);
		
		Document test = DomUtils.asDocument(html);
		assertNotNull(test);
		
		DOMComparer dc = new DOMComparer(control, test);
		
		List<Difference> differences = dc.compare();
		assertEquals("Found no differences in Documents",differences.size(),EMPTY);
	}
	
	@Test
	public void comparePartialDifference() throws IOException{
		String controlHTML = "<html><body><header>Crawljax</header><p>There are differences</p></body></html>";
		String testHTML = "<html><head><title>Crawljax</title></head><body><p>There are differences.</body></html>";
		final int EXPECTED_DIFF = 5;
		
		Document control = DomUtils.asDocument(controlHTML);
		assertNotNull(control);
		
		Document test = DomUtils.asDocument(testHTML);
		assertNotNull(test);
		
		DOMComparer dc = new DOMComparer(control, test);
		
		List<Difference> differences = dc.compare();
		assertEquals("Found 5 differences", differences.size(), EXPECTED_DIFF);

	}
	
}