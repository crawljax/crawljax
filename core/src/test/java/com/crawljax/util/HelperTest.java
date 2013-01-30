package com.crawljax.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.xml.transform.TransformerException;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.browser.WebDriverBackedEmbeddedBrowser;
import com.crawljax.test.BrowserTest;

/**
 * Test for the Helper class.
 */
@Category(BrowserTest.class)
public class HelperTest {

	private static final String INDEX = "src/test/resources/site/index.html";

	/**
	 * Test get document function.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testGetDocument() throws IOException {
		String html = "<html><body><p/></body></html>";
		Document doc = DomUtils.asDocument(html);
		assertNotNull(doc);
	}

	/**
	 * Test get document from browser function.
	 */
	@Test
	public void testGetDocumentFromBrowser() throws SAXException, IOException {
		// TODO Stefan; Refactor out the direct use of FirefoxDriver
		EmbeddedBrowser browser =
		        WebDriverBackedEmbeddedBrowser.withDriver(new FirefoxDriver(), null, 200, 300);
		File index = new File(INDEX);
		String html = "";
		browser.goToUrl(new URL("file://" + index.getAbsolutePath()));
		html = browser.getDom();
		assertNotNull(html);

		Document doc = DomUtils.asDocument(html);
		assertNotNull(doc);

		browser.close();
		browser = null;

	}

	@Test
	public void getElementAttributes() throws SAXException, IOException {
		Document dom;
		dom =
		        DomUtils.getDocumentNoBalance("<html><body><div class=\"bla\" "
		                + "id=\"test\">Bla</div></body></html>");
		assertEquals("class=bla id=test",
		        DomUtils.getAllElementAttributes(dom.getElementById("test")));
	}

	@Test
	public void writeAndGetContents() throws IOException, TransformerException, SAXException {
		File f = File.createTempFile("HelperTest.writeAndGetContents", ".tmp");
		DomUtils.writeDocumentToFile(
		        DomUtils.asDocument("<html><body><p>Test</p></body></html>"),
		        f.getAbsolutePath(), "html", 2);

		assertNotSame("", DomUtils.getTemplateAsString(f.getAbsolutePath()));

		assertTrue(f.exists());

	}
}
