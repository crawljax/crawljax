package com.crawljax.core.configuration;

import java.net.MalformedURLException;

import org.junit.Test;

import com.crawljax.core.CrawljaxController;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;

public class AddingMultipleURLsTest {
	
	@Test
	public void testMultipleURLs() throws MalformedURLException {
		CrawljaxConfigurationBuilder builder = CrawljaxConfiguration.builderFor(AddingMultipleURLsTest.class.getResource("/site/simple.html"));
		builder.alsoCrawl(new java.net.URL("http://google.com"));
		CrawljaxController crawljax = new CrawljaxController(builder.build());
	}
	
	@Test
	public void testMultipleStringURLs() {
		CrawljaxConfigurationBuilder builder = CrawljaxConfiguration.builderFor(AddingMultipleURLsTest.class.getResource("/site/simple.html"));
		builder.alsoCrawl("http://google.com");
		CrawljaxController crawljax = new CrawljaxController(builder.build());
	}
	
}
