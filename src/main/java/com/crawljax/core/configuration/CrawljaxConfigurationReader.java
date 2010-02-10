package com.crawljax.core.configuration;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.Configuration;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.condition.eventablecondition.EventableCondition;
import com.crawljax.core.TagElement;
import com.crawljax.core.plugin.Plugin;
import com.crawljax.util.PropertyHelper;

/**
 * Reader for CrawljaxConfiguration. For internal use only!
 * 
 * @author Danny
 * @version $Id$
 */
public class CrawljaxConfigurationReader {

	private final CrawljaxConfiguration crawljaxConfiguration;
	private final CrawlSpecificationReader crawlSpecificationReader;
	private final ThreadConfigurationReader threadConfigurationReader;

	/**
	 * Construct a new reader wrapper.
	 * 
	 * @param crawljaxConfiguration
	 *            The instance to wrap around.
	 */
	public CrawljaxConfigurationReader(CrawljaxConfiguration crawljaxConfiguration) {
		this.crawljaxConfiguration = crawljaxConfiguration;
		this.crawlSpecificationReader =
		        new CrawlSpecificationReader(crawljaxConfiguration.getCrawlSpecification());
		this.threadConfigurationReader =
		        new ThreadConfigurationReader(crawljaxConfiguration.getThreadConfiguration());
	}

	/**
	 * @return a PropertiesConfiguration. For use by PropertyHelper only!
	 */
	@Deprecated
	public Configuration getConfiguration() {
		return crawljaxConfiguration.getConfiguration();
	}

	/**
	 * TODO this call must be removed to maintain a "Reader"-only implementation.
	 * 
	 * @return a CrawljaxConfiguration. For use by cross-browser tester plugin!
	 */
	@Deprecated
	public CrawljaxConfiguration getCrawljaxConfiguration() {
		return crawljaxConfiguration;
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
	public BrowserType getBrowser() {
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

	/**
	 * @return the crawlSpecificationReader
	 */
	public CrawlSpecificationReader getCrawlSpecificationReader() {

		return crawlSpecificationReader;
	}

	/**
	 * @return a list of all included CrawlElements.
	 */
	public List<CrawlElement> getAllIncludedCrawlElements() {

		return crawljaxConfiguration.getAllIncludedCrawlElements();
	}

	/**
	 * @return a list of tag elements.
	 */
	public List<TagElement> getTagElements() {

		List<TagElement> tagelements = new ArrayList<TagElement>();

		String props = ConfigurationHelper.listToString(getAllIncludedCrawlElements());

		String[] tags = props.split(",");

		for (String text : tags) {

			TagElement tagElement = PropertyHelper.parseTagElement(text.trim());
			if (tagElement != null) {
				tagelements.add(tagElement);
			}
		}

		return tagelements;
	}

	/**
	 * @return a list of TagElements.
	 */
	public List<TagElement> getExcludeTagElements() {

		List<TagElement> tagelements = new ArrayList<TagElement>();

		String props =
		        ConfigurationHelper.listToString(crawljaxConfiguration.getCrawlSpecification()
		                .crawlActions().getCrawlElementsExcluded());
		String[] tags = props.split(",");

		for (String text : tags) {
			TagElement tagElement = PropertyHelper.parseTagElement(text.trim());
			if (tagElement != null) {
				tagelements.add(tagElement);
			}
		}

		return tagelements;

	}

	/**
	 * @return a list of attributes to be filtered from the DOM string.
	 */
	public List<String> getFilterAttributeNames() {
		return crawljaxConfiguration.getFilterAttributeNames();
	}

	/**
	 * @return the thread configruation.
	 */
	public ThreadConfigurationReader getThreadConfigurationReader() {
		return this.threadConfigurationReader;
	}
}
