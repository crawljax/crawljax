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

	private static String projectRelativePath = "project.path.relative";
	private static String outputFolderName = "output.path";
	private static String genFilepath = "generated.pages.filepath";
	private static String siteUrl = "site.url";
	private static String crawlDepth = "crawl.depth";
	private static String crawlMaxStates = "crawl.max.states";
	private static String crawlMaxTime = "crawl.max.runtime";
	private static String crawlThrehold = "crawl.threshold";
	private static String crawlNumberOfThreads = "crawl.numberOfThreads";

	// TODO danny, is this used?
	private static String robotEvents = "robot.events";

	private static String crawlTags = "crawl.tags";
	private static String crawlExludeTags = "crawl.tags.exclude";
	private static String crawlFilterAttributes = "crawl.filter.attributes";

	private static String hibernateProperties = "hibernate.properties";

	// TODO danny, is this used?
	private static String crawlManualEnterForm = "crawl.forms.manual";
	private static String crawlFormRandomInput = "crawl.forms.randominput";

	private static String formProperties = "forms.properties";

	private static String browser = "browser";

	private static String crawlWaitReload = "crawl.wait.reload";
	private static String crawlWaitEvent = "crawl.wait.event";
	private static String hibernateSchema = "hibernate.hbm2ddl.auto";
	private static String useDatabase = "database.use";
	// if each candidate clickable should be clicked only once
	private static String clickOnce = "click.once";

	// TODO danny, can these be removed?
	private static String debugVariables = "reportbuilder.debugvariables";
	private static String detectEventHandlers = "eventHandlers.detect";
	private static String supportDomEvents = "eventHandlers.supportDomEvents";
	private static String supportAddEvents = "eventHandlers.supportAddEvents";
	private static String supportJQuery = "eventHandlers.supportJQuery";

	private static String genFilepathValue = "target/generated-sources/";

	private static String proxyEnabled = "proxy.enabled";

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
		if (file.containsKey(outputFolderName)) {
			config.setOutputFolder(file.getString(outputFolderName));
		}
		config.setProjectRelativePath(file.getString(projectRelativePath));

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

		CrawlSpecification crawler = new CrawlSpecification(file.getString(siteUrl));

		/*
		 * TODO: use getBoolean. we use getInt for backward compatibility. in the future we can use
		 * file.getBoolean
		 */
		crawler.setClickOnce(file.getInt(clickOnce) == 1);
		crawler.setDepth(file.getInt(crawlDepth));

		crawler.setMaximumStates(file.getInt(crawlMaxStates));
		crawler.setMaximumRuntime(file.getInt(crawlMaxTime));

		crawler.setWaitTimeAfterEvent(file.getInt(crawlWaitEvent));
		crawler.setWaitTimeAfterReloadUrl(file.getInt(crawlWaitReload));

		crawler.setRandomInputInForms(file.getInt(crawlFormRandomInput) == 1);

		crawler.setNumberOfThreads(file.getInt(crawlNumberOfThreads));

		return crawler;
	}

	/**
	 * @return The configuration object that represents the file contents.
	 */
	public CrawljaxConfiguration getConfiguration() {
		return config;
	}
}
