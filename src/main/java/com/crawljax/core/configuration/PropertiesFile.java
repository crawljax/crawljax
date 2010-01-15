package com.crawljax.core.configuration;

import java.io.File;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 * This class is used to create a CrawljaxConfiguration object configured with settings from a file.
 * 
 * @author Frank Groeneveld
 * @version $Id$
 */
public class PropertiesFile {

	private CrawljaxConfiguration config;

	private static final String PROJECTRELATIVEPATH = "project.path.relative";
	private static final String OUTPUTFOLDERNAME = "output.path";
	private static final String SITEURL = "site.url";
	private static final String CRAWLDEPTH = "crawl.depth";
	private static final String CRAWLMAXSTATES = "crawl.max.states";
	private static final String CRAWLMAXTIME = "crawl.max.runtime";
	private static final String CRAWLNUMBEROFTHREADS = "crawl.numberOfThreads";

	private static final String CRAWLTAGS = "crawl.tags";
	private static final String CRAWLEXCLUDETAGS = "crawl.tags.exclude";
	private static final String CRAWLFILTERATTRIBUTES = "crawl.filter.attributes";

	private static String HIBERNATEPROPERTIES = "hibernate.properties";

	private static final String CRAWLFORMRANDOMINPUT = "crawl.forms.randominput";

	private static final String formProperties = "forms.properties";

	private static final String browser = "browser";

	private static final String crawlWaitReload = "crawl.wait.reload";
	private static final String crawlWaitEvent = "crawl.wait.event";
	private static final String hibernateSchema = "hibernate.hbm2ddl.auto";
	private static final String useDatabase = "database.use";
	// if each candidate clickable should be clicked only once
	private static String clickOnce = "click.once";

	private static final String proxyEnabled = "proxy.enabled";

	/**
	 * default is Firefox.
	 */
	private static String browserValue = "firefox";

	/**
	 * Initialize and read out properties from filename.
	 * 
	 * @param filename
	 *            Name of the properties file.
	 * @throws ConfigurationException
	 *             On errors in properties file.
	 */
	public PropertiesFile(String filename) throws ConfigurationException {
		config = new CrawljaxConfiguration();

		File f = new File(filename);
		if (!f.exists()) {
			throw new ConfigurationException("Configuration file not found: " + filename);
		}

		read(new PropertiesConfiguration(filename));
	}

	/**
	 * Read properties from the config file and set them in the config object.
	 * 
	 * @param file
	 *            The properties file.
	 */
	private void read(PropertiesConfiguration file) {
		if (file.containsKey(OUTPUTFOLDERNAME)) {
			config.setOutputFolder(file.getString(OUTPUTFOLDERNAME));
		}
		config.setProjectRelativePath(file.getString(PROJECTRELATIVEPATH));

		config.setCrawlSpecification(getCrawlSpecification(file));

		if (file.containsKey(proxyEnabled) && file.getBoolean(proxyEnabled)) {
			config.setProxyConfiguration(new ProxyConfiguration());
		}

	}

	/**
	 * @param file
	 *            Properties file.
	 * @return The CrawljaxConfiguration object that represents the file.
	 */
	private CrawlSpecification getCrawlSpecification(PropertiesConfiguration file) {

		CrawlSpecification crawler = new CrawlSpecification(file.getString(SITEURL));

		/*
		 * TODO: use getBoolean. we use getInt for backward compatibility. in the future we can use
		 * file.getBoolean
		 */
		crawler.setClickOnce(file.getInt(clickOnce) == 1);
		crawler.setDepth(file.getInt(CRAWLDEPTH));

		crawler.setMaximumStates(file.getInt(CRAWLMAXSTATES));
		crawler.setMaximumRuntime(file.getInt(CRAWLMAXTIME));

		crawler.setWaitTimeAfterEvent(file.getInt(crawlWaitEvent));
		crawler.setWaitTimeAfterReloadUrl(file.getInt(crawlWaitReload));

		crawler.setRandomInputInForms(file.getInt(CRAWLFORMRANDOMINPUT) == 1);

		crawler.setNumberOfThreads(file.getInt(CRAWLNUMBEROFTHREADS));

		return crawler;
	}

	/**
	 * @return The configuration object that represents the file contents.
	 */
	public CrawljaxConfiguration getConfiguration() {
		return config;
	}
}
