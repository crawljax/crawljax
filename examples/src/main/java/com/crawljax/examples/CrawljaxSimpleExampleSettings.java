package com.crawljax.examples;

import org.apache.commons.configuration.ConfigurationException;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.core.CrawljaxController;
import com.crawljax.core.configuration.CrawlSpecification;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.InputSpecification;
import com.crawljax.core.configuration.ThreadConfiguration;

/**
 * Simple Example.
 */
public final class CrawljaxSimpleExampleSettings {

	private static final String URL = "http://www.google.com";
	private static final int MAX_DEPTH = 2;
	private static final int MAX_NUMBER_STATES = 8;

	private CrawljaxSimpleExampleSettings() {

	}

	private static CrawljaxConfiguration getCrawljaxConfiguration() {
		CrawljaxConfiguration config = new CrawljaxConfiguration();
		config.setCrawlSpecification(getCrawlSpecification());
		config.setThreadConfiguration(getThreadConfiguration());
		config.setBrowser(BrowserType.firefox);
		return config;
	}

	private static ThreadConfiguration getThreadConfiguration() {
		ThreadConfiguration tc = new ThreadConfiguration();
		tc.setBrowserBooting(true);
		tc.setNumberBrowsers(1);
		tc.setNumberThreads(1);
		return tc;
	}

	private static CrawlSpecification getCrawlSpecification() {
		CrawlSpecification crawler = new CrawlSpecification(URL);
		crawler.setRandomInputInForms(false);
		// click these elements

		crawler.click("a");
		crawler.click("button");

		// except these
		crawler.dontClick("a").underXPath("//DIV[@id='guser']");
		crawler.dontClick("a").withText("Language Tools");

		crawler.setInputSpecification(getInputSpecification());

		// limit the crawling scope
		crawler.setMaximumStates(MAX_NUMBER_STATES);
		crawler.setDepth(MAX_DEPTH);

		return crawler;
	}

	private static InputSpecification getInputSpecification() {
		InputSpecification input = new InputSpecification();
		input.field("gbqfq").setValue("Crawljax");
		return input;
	}

	/**
	 * @param args
	 *            the command line args
	 */
	public static void main(String[] args) throws ConfigurationException {
		CrawljaxController crawljax = new CrawljaxController(getCrawljaxConfiguration());
		crawljax.run();
	}

}
