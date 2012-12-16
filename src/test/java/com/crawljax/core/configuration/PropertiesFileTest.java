package com.crawljax.core.configuration;

import org.junit.Test;

import com.crawljax.core.CrawljaxController;
import com.crawljax.test.BrowserTest;

public class PropertiesFileTest implements BrowserTest {

	@Test
	public void readProperties() throws Exception {
		PropertiesFile p =
		        new PropertiesFile("src/test/resources/configuration/crawljax.properties");

		CrawljaxConfiguration config = p.getConfiguration();
		CrawljaxController controller = new CrawljaxController(config);
		controller.run();

	}
}
