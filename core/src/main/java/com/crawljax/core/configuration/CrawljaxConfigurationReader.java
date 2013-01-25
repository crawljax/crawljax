package com.crawljax.core.configuration;

import java.util.List;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.browser.EmbeddedBrowserBuilder;
import com.crawljax.condition.eventablecondition.EventableCondition;
import com.crawljax.core.TagAttribute;
import com.crawljax.core.TagElement;
import com.crawljax.core.plugin.Plugin;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableSet;

/**
 * Reader for CrawljaxConfiguration. For internal use only!
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
	public ImmutableList<EventableCondition> getEventableConditions() {
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
	public ImmutableList<CrawlElement> getAllIncludedCrawlElements() {

		return crawljaxConfiguration.getAllIncludedCrawlElements();
	}

	/**
	 * Convert getAllIncludedCrawlElements to a list of TagElements. TODO: Completely skip this step
	 * by using either CrawlElement or TagElement everywhere.
	 * 
	 * @return A list of tag elements.
	 */
	public ImmutableList<TagElement> getTagElements() {
		return asTagElementList(getAllIncludedCrawlElements());
	}

	/**
	 * Convert getCrawlElementsExcluded to a list of TagElements. TODO: Completely skip this step by
	 * using either CrawlElement or TagElement everywhere.
	 * 
	 * @return a list of TagElements.
	 */
	public ImmutableList<TagElement> getExcludeTagElements() {

		ImmutableList<CrawlElement> excluded = crawljaxConfiguration.getCrawlSpecification()
		        .crawlActions().getCrawlElementsExcluded();
		return asTagElementList(excluded);

	}

	private ImmutableList<TagElement> asTagElementList(ImmutableList<CrawlElement> excluded) {
		Builder<TagElement> resultList = ImmutableList.builder();

		for (CrawlElement crawlElement : excluded) {
			ImmutableSet.Builder<TagAttribute> attributes = ImmutableSet.builder();

			for (CrawlAttribute crawlAttribute : crawlElement.getCrawlAttributes()) {
				attributes.add(new TagAttribute(crawlAttribute.getName(), crawlAttribute
				        .getValue()));
			}

			TagElement tagElement =
			        new TagElement(attributes.build(), crawlElement.getTagName(),
			                crawlElement.getId());

			resultList.add(tagElement);
		}

		return resultList.build();
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
