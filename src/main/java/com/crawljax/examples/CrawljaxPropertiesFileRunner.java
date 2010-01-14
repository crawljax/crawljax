package com.crawljax.examples;

import org.apache.commons.configuration.ConfigurationException;

import com.crawljax.core.CrawljaxController;
import com.crawljax.core.CrawljaxException;
import com.crawljax.util.PropertyHelper;

/**
 * Default Crawljax runner class that loads its settings from a properties file.
 * 
 * @author Frank Groeneveld <frankgroeneveld@gmail.com>
 */
public final class CrawljaxPropertiesFileRunner {

	private CrawljaxPropertiesFileRunner() {

	}

	/**
	 * @param args
	 *            Console arguments.
	 */
	public static void main(String[] args) {
		if (args.length > 0) {

			try {
				PropertyHelper.init(args[0]);
			} catch (ConfigurationException e) {
				System.err.println("Error when loading properties file.");
				e.printStackTrace();
				System.exit(1);
			}
		} else {
			try {
				PropertyHelper.init("crawljax.properties");
			} catch (ConfigurationException e) {
				System.err.println("Error when loading properties file.");
				e.printStackTrace();
				System.exit(1);
			}
		}

		try {
			CrawljaxController crawljax = new CrawljaxController();
			crawljax.run();
		} catch (CrawljaxException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (ConfigurationException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}
