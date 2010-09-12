package com.crawljax.core.configuration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.crawljax.browser.EmbeddedBrowserBuilder;
import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.condition.eventablecondition.EventableCondition;
import com.crawljax.core.TagAttribute;
import com.crawljax.core.TagElement;
import com.crawljax.core.plugin.Plugin;

/**
 * Reader for CrawljaxConfiguration. For internal use only!
 * 
 * @author Danny
 * @version $Id: CrawljaxConfigurationReader.java 233 2010-02-10 15:22:34Z lenselinkstefan@gmail.com
 *          $
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
	 * @return the browser builder used.
	 */
	public EmbeddedBrowserBuilder getBrowserBuilder() {
		return crawljaxConfiguration.getBrowserBuilder();
	}

	/**
	 * @return the url of the remote hub that must be used.
	 */
	public String getRemoteHubUrl() {
		return crawljaxConfiguration.getRemoteHubUrl();
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
	 * Convert getAllIncludedCrawlElements to a list of TagElements. TODO: Completely skip this step
	 * by using either CrawlElement or TagElement everywhere.
	 * 
	 * @return A list of tag elements.
	 */
	public List<TagElement> getTagElements() {
		List<TagElement> tagElements = new ArrayList<TagElement>();

		for (CrawlElement crawlElement : getAllIncludedCrawlElements()) {
			Set<TagAttribute> attributes = new HashSet<TagAttribute>();

			for (CrawlAttribute crawlAttribute : crawlElement.getCrawlAttributes()) {
				TagAttribute tag =
				        new TagAttribute(crawlAttribute.getName(), crawlAttribute.getValue());
				attributes.add(tag);
			}

			TagElement tagElement = new TagElement(attributes, crawlElement.getTagName());

			tagElement.setId(crawlElement.getId());

			tagElements.add(tagElement);
		}

		return tagElements;
	}

	/**
	 * Convert getCrawlElementsExcluded to a list of TagElements. TODO: Completely skip this step by
	 * using either CrawlElement or TagElement everywhere.
	 * 
	 * @return a list of TagElements.
	 */
	public List<TagElement> getExcludeTagElements() {
		List<TagElement> tagElements = new ArrayList<TagElement>();

		for (CrawlElement crawlElement : crawljaxConfiguration.getCrawlSpecification()
		        .crawlActions().getCrawlElementsExcluded()) {
			Set<TagAttribute> attributes = new HashSet<TagAttribute>();

			for (CrawlAttribute crawlAttribute : crawlElement.getCrawlAttributes()) {
				TagAttribute tag =
				        new TagAttribute(crawlAttribute.getName(), crawlAttribute.getValue());
				attributes.add(tag);
			}

			TagElement tagElement = new TagElement(attributes, crawlElement.getTagName());

			tagElement.setId(crawlElement.getId());

			tagElements.add(tagElement);
		}

		return tagElements;

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
