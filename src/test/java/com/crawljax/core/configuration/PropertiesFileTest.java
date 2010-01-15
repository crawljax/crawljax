package com.crawljax.core.configuration;

import static org.junit.Assert.fail;

import org.apache.commons.configuration.ConfigurationException;
import org.junit.Test;

import com.crawljax.core.CrawljaxController;
import com.crawljax.core.CrawljaxException;

public class PropertiesFileTest {

	@Test
	public void readProperties() {
		try {
			PropertiesFile p =
			        new PropertiesFile(
			                "src/test/java/com/crawljax/core/configuration/crawljax.properties");

			CrawljaxConfiguration config = p.getConfiguration();
			CrawljaxController controller = new CrawljaxController(config);
			controller.run();
		} catch (ConfigurationException e) {
			fail(e.getMessage());
		} catch (CrawljaxException e) {
			fail(e.getMessage());
		}

	}
}
