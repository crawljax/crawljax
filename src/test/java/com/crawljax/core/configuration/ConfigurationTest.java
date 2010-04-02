package com.crawljax.core.configuration;

import static org.junit.Assert.assertEquals;

import org.apache.commons.configuration.ConfigurationException;
import org.junit.Test;

/**
 * @author droest@google.com (Your Name Here)
 */
public class ConfigurationTest {

	private static final String OUTPUT_FOLDER_WITHOUT_SLASH = "temp";
	private static final String OUTPUT_FOLDER_WITH_SLASH = "temp/";

	@Test
	public void testOutputFolder() throws ConfigurationException {
		CrawljaxConfiguration config = new CrawljaxConfiguration();
		config.setCrawlSpecification(new CrawlSpecification("http://www.crawljax.com"));
		config.setOutputFolder(OUTPUT_FOLDER_WITHOUT_SLASH);
		assertEquals(OUTPUT_FOLDER_WITH_SLASH, config.getOutputFolder());
	}

}
