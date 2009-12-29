package com.crawljax.core.configuration;

import java.util.List;


import org.apache.commons.configuration.Configuration;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.condition.eventablecondition.EventableCondition;
import com.crawljax.core.plugin.Plugin;

/**
 * Reader for CrawljaxConfiguration. For internal use only!
 * 
 * @author Danny
 */
public class CrawljaxConfigurationReader {

	private final CrawljaxConfiguration crawljaxConfiguration;

	/**
	 * Construct a new reader wrapper.
	 * 
	 * @param crawljaxConfiguration
	 *            The instance to wrap around.
	 */
	public CrawljaxConfigurationReader(CrawljaxConfiguration crawljaxConfiguration) {
		this.crawljaxConfiguration = crawljaxConfiguration;
	}

	/**
	 * @return The crawl specification.
	 */
	public CrawlSpecification getCrawlSpecification() {
		return crawljaxConfiguration.getCrawlSpecification();
	}

	/**
	 * @return a PropertiesConfiguration. For use by PropertyHelper only!
	 */
	public Configuration getConfiguration() {
		return crawljaxConfiguration.getConfiguration();
	}

	/**
	 * @return The eventable conditions.
	 */
	public List<EventableCondition> getEventableConditions() {
		return crawljaxConfiguration.getEventableConditions();
	}

	/**
	 * @return The browser.
	 */
	public EmbeddedBrowser getBrowser() {
		return crawljaxConfiguration.getBrowser();
	}

	/**
	 * @return The hibernate configuration.
	 */
	public HibernateConfiguration getHibernateConfiguration() {
		return crawljaxConfiguration.getHibernateConfiguration();
	}

	/**
	 * @return The input specification.
	 */
	public InputSpecification getInputSpecification() {
		return crawljaxConfiguration.getInputSpecification();
	}

	/**
	 * @return a list of plugins.
	 */
	public List<Plugin> getPlugins() {
		return crawljaxConfiguration.getPlugins();
	}

	/**
	 * @return Whether to use the database.
	 */
	public boolean getUseDatabase() {
		return crawljaxConfiguration.getUseDatabase();
	}

	/**
	 * Method to get the proxy configuration object.
	 * 
	 * @return The proxy configuration object.
	 */
	public ProxyConfiguration getProxyConfiguration() {
		return crawljaxConfiguration.getProxyConfiguration();
	}

}
