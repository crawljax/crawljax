package com.crawljax.core.configuration;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.crawljax.core.CrawljaxController;
import com.crawljax.test.BrowserTest;

@Category(BrowserTest.class)
public class PropertiesFileTest {

	@Test
	public void readProperties() throws Exception {
		PropertiesFile p =
		        new PropertiesFile("src/test/resources/configuration/crawljax.properties");

		CrawljaxConfiguration config = p.getConfiguration();
		CrawljaxController controller = new CrawljaxController(config);
		controller.run();

	}
}
