package com.crawljax.plugins.crawloverview.example;

import com.crawljax.core.CrawljaxController;
import com.crawljax.core.configuration.CrawlSpecification;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.plugins.crawloverview.CrawlOverview;
import com.google.common.io.Files;

public class CrawlOverviewExample {

	private static final String URL = "http://crawljax.com/";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		CrawljaxConfiguration config = new CrawljaxConfiguration();
		CrawlSpecification crawler = new CrawlSpecification(URL);
		crawler.setMaximumStates(5);
		crawler.clickDefaultElements();
		config.setCrawlSpecification(crawler);
		config.addPlugin(new CrawlOverview(Files.createTempDir()));
		try {
			CrawljaxController crawljax = new CrawljaxController(config);
			crawljax.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
