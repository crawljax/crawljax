package com.crawljax.core.configuration;

import org.junit.Test;

import com.crawljax.core.CrawljaxController;

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
