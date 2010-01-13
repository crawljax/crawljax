package com.crawljax.core.configuration;

import static org.junit.Assert.fail;

import org.apache.commons.configuration.ConfigurationException;
import org.junit.Test;

public class PropertiesFileTest {

	@Test
	public void readProperties() {
		try {
			PropertiesFile p =
			        new PropertiesFile(
			                "src/test/java/com/crawljax/core/configuration/crawljax.properties");

			CrawljaxConfiguration c = p.getConfiguration();

		} catch (ConfigurationException e) {
			fail(e.getMessage());
		}

	}
}
