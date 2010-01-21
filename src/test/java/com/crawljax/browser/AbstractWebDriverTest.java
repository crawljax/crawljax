package com.crawljax.browser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.crawljax.core.CrawljaxException;
import com.crawljax.util.Helper;

public class AbstractWebDriverTest {

	@Test
	public void testGetDocument() {
		File index = new File("src/test/site/iframe/index.html");
		AbstractWebDriver driver = new WebDriverFirefox();

		Document doc;
		try {
			driver.goToUrl("file://" + index.getAbsolutePath());
			doc = Helper.getDocument(driver.getDom());
			NodeList frameNodes = doc.getElementsByTagName("IFRAME");
			assertEquals(3, frameNodes.getLength());

		} catch (SAXException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (CrawljaxException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		driver.close();

	}
}
