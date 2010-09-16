package com.crawljax.examples;

import com.crawljax.core.CrawljaxController;
import com.crawljax.core.CrawljaxException;
import com.crawljax.core.configuration.CrawlSpecification;
import com.crawljax.core.configuration.CrawljaxConfiguration;

import org.apache.commons.configuration.ConfigurationException;

/**
 * @author frank, mesbah
 */
public final class JarRunner {

	private JarRunner() {

	}

	/**
	 * @param args
	 *            URL.
	 */
	public static void main(String[] args) {
		if (args.length < 1) {
			System.err.println("Please give an url as first argument to Crawljax");
			System.exit(1);
		}
		CrawlSpecification crawler = new CrawlSpecification(args[0]);
		crawler.clickDefaultElements();

		CrawljaxConfiguration config = new CrawljaxConfiguration();

		config.setCrawlSpecification(crawler);

		try {
			CrawljaxController crawljax = new CrawljaxController(config);
			crawljax.run();
		} catch (ConfigurationException e) {
			e.printStackTrace();
		} catch (CrawljaxException e) {
			e.printStackTrace();
		}
	}
}
