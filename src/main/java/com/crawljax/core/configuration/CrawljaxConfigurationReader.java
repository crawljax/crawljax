package com.crawljax.core.configuration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

			TagElement tagElement = parseTagElement(text.trim());
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
			TagElement tagElement = parseTagElement(text.trim());
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

	/**
	 * Parses the tag elements. This has to be deprecated, because its an old part of
	 * PropertyHelper.
	 * 
	 * @param text
	 *            The string containing the tag elements.
	 * @return The tag element.
	 */
	private TagElement parseTagElement(String text) {
		if (text.equals("")) {
			return null;
		}
		String name = null;
		Set<TagAttribute> attributes = new HashSet<TagAttribute>();
		String id = null;

		Pattern pattern =
		        Pattern.compile("\\w+:\\{(\\w+=?(\\-*\\s*[\\w%]\\s*)+\\;?\\s?)*}"
		                + "(\\[\\w+\\])?");

		Pattern patternTagName = Pattern.compile("\\w+");

		Pattern patternAttributes = Pattern.compile("\\{(\\w+=(\\-*\\s*[\\w%]\\s*)+\\;?\\s?)*}");

		Pattern patternAttribute = Pattern.compile("(\\w+)=((\\-*\\s*[\\w%]\\s*)+)");

		Pattern patternId = Pattern.compile("(\\[)(\\w+)(\\])");

		Matcher matcher = pattern.matcher(text);

		if (matcher.matches()) {
			String substring = matcher.group();
			matcher = patternTagName.matcher(substring);

			if (matcher.find()) {
				name = matcher.group().trim();
			}

			matcher = patternAttributes.matcher(substring);

			// attributes
			if (matcher.find()) {
				String tmp = matcher.group();
				// parse attributes
				matcher = patternAttribute.matcher(tmp);

				while (matcher.find()) {
					String attrName = matcher.group(1).trim();
					String value = matcher.group(2).trim();
					attributes.add(new TagAttribute(attrName, value));
				}
			}

			// id
			matcher = patternId.matcher(substring);
			if (matcher.find()) {
				id = matcher.group(2);
			}

			TagElement el = new TagElement(attributes, name);
			if (id != null) {
				el.setId(id);
			}
			return el;
		}

		return null;
	}
}
