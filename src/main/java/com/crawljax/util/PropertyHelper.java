/**
 * Created Dec 20, 2007
 */
package com.crawljax.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

import com.crawljax.core.TagAttribute;
import com.crawljax.core.TagElement;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawljaxConfigurationReader;

/**
 * @author mesbah
 * @version $Id$
 */
public final class PropertyHelper {
	private static final Logger LOGGER = Logger.getLogger(PropertyHelper.class.getName());

	private static CrawljaxConfigurationReader crawljaxConfiguration;

	private static String projectRelativePath = "project.path.relative";
	private static String projectRelativePathValue = "";

	private static String outputFolderName = "output.path";
	private static String outputFolder = "";

	private static String siteUrl = "site.url";
	private static String baseUrl = "site.base.url";
	private static String crawlDepth = "crawl.depth";
	private static String crawlMaxStates = "crawl.max.states";
	private static String crawlMaxTime = "crawl.max.runtime";

	private static String robotEvents = "robot.events";
	private static String crawlTags = "crawl.tags";
	private static String crawlExludeTags = "crawl.tags.exclude";
	private static String crawlFilterAttributes = "crawl.filter.attributes";
	private static String crawlNumberOfThreads = "crawl.numberOfThreads";

	private static String hibernateProperties = "hibernate.properties";
	private static String hibernatePropertiesValue = "hibernate.properties";

	private static String crawlFormRandomInput = "crawl.forms.randominput";
	private static int crawlFormRandomInputValue = 1;

	private static String formProperties = "forms.properties";
	private static int crawlManualEnterFormValue = 1;
	private static String formPropertiesValue = "forms.properties";

	private static String browser = "browser";
	private static String crawlWaitReload = "crawl.wait.reload";
	private static String crawlWaitEvent = "crawl.wait.event";
	private static Configuration config;
	private static String hibernateSchema = "hibernate.hbm2ddl.auto";
	private static String hibernateSchemaValue;

	private static String useDatabase = "database.use";
	private static int useDatabaseValue = 0;

	// if each candidate clickable should be clicked only once
	private static String clickOnce = "click.once";
	private static int clickOnceValue = 1;

	private static int testInvariantsWhileCrawlingValue = 1;

	/* event handlers */
	private static String detectEventHandlers = "eventHandlers.detect";
	private static int detectEventHandlersValue = 1;

	private static String siteUrlValue;
	private static String baseUrlValue;
	private static int crawlDepthValue;
	private static List<String> robotEventsValues;
	private static List<String> crawlTagsValues;
	private static List<String> crawlFilterAttributesValues;
	private static List<TagElement> crawlTagElements = new ArrayList<TagElement>();
	private static List<TagElement> crawlExcludeTagElements = new ArrayList<TagElement>();
	private static List<String> atusaPluginsValues;
	private static int crawlMaxStatesValue = 0;
	private static int crawlMaxTimeValue = 0;

	private static String proxyEnabled = "proxy.enabled";

	private static int proxyEnabledValue = 0;

	private static int domTidyValue = 0;

	private static int crawNumberOfThreadsValue = 1;

	private static String maxHistorySizeText = "history.maxsize";

	private static int maxHistorySize;

	private static String propertiesFileName;

	/**
	 * default is IE.
	 */
	private static String browserValue = "ie";
	private static int crawlWaitReloadValue;
	private static int crawlWaitEventValue;

	private PropertyHelper() {
	}

	/**
	 * @param propertiesFile
	 *            thie properties file.
	 * @throws ConfigurationException
	 *             if configuration fails.
	 */
	public static void init(String propertiesFile) throws ConfigurationException {
		PropertyHelper.propertiesFileName = propertiesFile;
		crawljaxConfiguration = null;
		init(new PropertiesConfiguration(propertiesFile));
	}

	/**
	 * Initialize property helper with a CrawljaxConfiguration instance.
	 * 
	 * @param crawljaxConfiguration
	 *            The CrawljaxConfiguration instance.
	 * @throws ConfigurationException
	 *             On error.
	 */
	public static void init(CrawljaxConfiguration crawljaxConfiguration)
	        throws ConfigurationException {
		PropertyHelper.crawljaxConfiguration =
		        new CrawljaxConfigurationReader(crawljaxConfiguration);
		if (PropertyHelper.crawljaxConfiguration.getConfiguration() == null) {
			throw new ConfigurationException("Configuration cannot be null!");
		}

		init(PropertyHelper.crawljaxConfiguration.getConfiguration());
	}

	private static void init(Configuration configuration) throws ConfigurationException {
		config = configuration;
		/* reset crawltagelements */
		crawlTagElements = new ArrayList<TagElement>();
		if (config.containsKey(outputFolderName)) {
			outputFolder = getProperty(outputFolderName);
		}
		projectRelativePathValue = getProperty(projectRelativePath);

		siteUrlValue = getProperty(siteUrl);
		baseUrlValue = getProperty(baseUrl);
		crawlDepthValue = getPropertyAsInt(crawlDepth);
		crawNumberOfThreadsValue = getPropertyAsInt(crawlNumberOfThreads);
		// crawlThreholdValue = getPropertyAsDouble(crawlThrehold);
		robotEventsValues = getPropertyAsList(robotEvents);
		crawlTagsValues = getPropertyAsList(crawlTags);
		crawlFilterAttributesValues = getPropertyAsList(crawlFilterAttributes);
		browserValue = getProperty(browser);
		crawlWaitReloadValue = getPropertyAsInt(crawlWaitReload);
		crawlWaitEventValue = getPropertyAsInt(crawlWaitEvent);
		crawlMaxStatesValue = getPropertyAsInt(crawlMaxStates);
		crawlMaxTimeValue = getPropertyAsInt(crawlMaxTime);
		// crawlManualEnterFormValue =
		// getPropertyAsInt(crawlManualEnterForm);
		formPropertiesValue = getProperty(formProperties);
		if (config.containsKey(crawlFormRandomInput)) {
			crawlFormRandomInputValue = getPropertyAsInt(crawlFormRandomInput);
		}
		hibernatePropertiesValue = getProperty(hibernateProperties);

		useDatabaseValue = getPropertyAsInt(useDatabase);

		if (config.containsKey(clickOnce)) {
			clickOnceValue = getPropertyAsInt(clickOnce);
		}

		setTagElements();
		setTagExcludeElements();

		if (config.containsKey(proxyEnabled)) {
			proxyEnabledValue = getPropertyAsInt(proxyEnabled);
		}

		if (config.containsKey(maxHistorySizeText)) {
			maxHistorySize = getPropertyAsInt(maxHistorySizeText);
		}

		hibernateSchemaValue = getProperty(hibernateSchema);

		if (!checkProperties()) {
			LOGGER.error("Check the properties!");
			throw new ConfigurationException("Check the properties!");
		}
	}

	private static void setTagElements() {
		for (String text : getPropertyAsList(crawlTags)) {
			TagElement tagElement = parseTagElements(text);

			if (tagElement != null) {
				crawlTagElements.add(tagElement);
			}
		}
	}

	private static void setTagExcludeElements() {
		for (String text : getPropertyAsList(crawlExludeTags)) {
			TagElement tagElement = parseTagElements(text);

			if (tagElement != null) {
				crawlExcludeTagElements.add(tagElement);
			}
		}
	}

	/**
	 * @param property
	 *            the property.
	 * @return the value as string.
	 */
	public static String getProperty(String property) {
		return config.getString(property);
	}

	/**
	 * Check all the properties.
	 * 
	 * @return true if all properties are set.
	 */
	private static boolean checkProperties() {
		if (isEmpty(siteUrlValue)) {
			LOGGER.error("Property " + siteUrl + " is not set.");

			return false;
		}
		/*
		 * if (isEmpty(genFilepathValue)) { LOGGER.error("Property " + genFilepath +
		 * " is not set."); return false; }
		 */

		if (isEmpty("" + crawlDepthValue)) {
			LOGGER.error("Property " + crawlDepth + " is not set.");

			return false;
		}

		/*
		 * if (isEmpty("" + crawlThreholdValue)) { LOGGER.error("Property " + crawlThrehold +
		 * " is not set."); return false; }
		 */

		if (isEmpty(browserValue) && getCrawljaxConfiguration() == null) {
			LOGGER.error("Property " + browser + " is not set.");

			return false;
		}

		if (isEmpty("" + crawlWaitReloadValue)) {
			LOGGER.error("Property " + crawlWaitReload + " is not set.");

			return false;
		}

		if (isEmpty("" + crawlWaitEventValue)) {
			LOGGER.error("Property " + crawlWaitEvent + " is not set.");

			return false;
		}

		if (isEmpty(hibernateSchemaValue)) {
			LOGGER.error("Property " + hibernateSchema + " is not set.");

			return false;
		}

		return true;
	}

	private static boolean isEmpty(String property) {
		if ((property == null) || "".equals(property)) {
			return true;
		}

		return false;
	}

	/**
	 * @param property
	 *            name of the property.
	 * @return the value as an int.
	 */
	public static int getPropertyAsInt(String property) {
		return config.getInt(property);
	}

	/**
	 * @param property
	 *            the property.
	 * @return the value as a double.
	 */
	public static double getPropertyAsDouble(String property) {
		return config.getDouble(property);
	}

	/**
	 * @param property
	 *            the property.
	 * @return the values as a List.
	 */
	public static List<String> getPropertyAsList(String property) {
		List<String> result = new ArrayList<String>();
		String[] array = config.getStringArray(property);

		for (int i = 0; i < array.length; i++) {
			result.add(array[i]);
		}

		return result;
	}

	/**
	 * @return A reader for the CrawljaxConfiguration instance.
	 */
	public static CrawljaxConfigurationReader getCrawljaxConfiguration() {
		return crawljaxConfiguration;
	}

	/**
	 * @return The project relative path.
	 */
	public static String getProjectRelativePathValue() {
		return projectRelativePathValue;
	}

	/**
	 * @return The output folder with a trailing slash.
	 */
	public static String getOutputFolder() {
		return Helper.addFolderSlashIfNeeded(outputFolder);
	}

	/**
	 * @return the siteUrl
	 */
	public static String getSiteUrl() {
		return siteUrl;
	}

	/**
	 * @return the baseUrl
	 */
	public static String getBaseUrl() {
		return baseUrl;
	}

	/**
	 * @return the crawlDepth
	 */
	public static String getCrawlDepth() {
		return crawlDepth;
	}

	/**
	 * @return the crawlTags
	 */
	public static String getCrawlTags() {
		return crawlTags;
	}

	/**
	 * @return the browser
	 */
	public static String getBrowser() {
		return browser;
	}

	/**
	 * @return the config
	 */
	public static Configuration getConfig() {
		return config;
	}

	/**
	 * @return the siteUrlValue
	 */
	public static String getSiteUrlValue() {
		return siteUrlValue;
	}

	/**
	 * @return the baseUrlValue
	 */
	public static String getBaseUrlValue() {
		return baseUrlValue;
	}

	/**
	 * @return the crawlDepthValue
	 */
	public static int getCrawlDepthValue() {
		return crawlDepthValue;
	}

	/**
	 * @return the robotEventsValues
	 */
	public static List<String> getRobotEventsValues() {
		return robotEventsValues;
	}

	/**
	 * @return the crawlTagsValues
	 */
	public static List<String> getCrawlTagsValues() {
		return crawlTagsValues;
	}

	/**
	 * @return the crawlFilterAttributesValues
	 */
	public static List<String> getCrawlFilterAttributesValues() {
		return crawlFilterAttributesValues;
	}

	/**
	 * @return the browserValue
	 */
	public static String getBrowserValue() {
		return browserValue;
	}

	/**
	 * @return the crawlWaitReloadValue
	 */
	public static int getCrawlWaitReloadValue() {
		return crawlWaitReloadValue;
	}

	/**
	 * @return the crawlWaitEventValue
	 */
	public static int getCrawlWaitEventValue() {
		return crawlWaitEventValue;
	}

	/**
	 * @return TODO: DOCUMENT ME!
	 */
	public static String getCrawlMaxStates() {
		return crawlMaxStates;
	}

	/**
	 * @return TODO: DOCUMENT ME!
	 */
	public static String getCrawlMaxTime() {
		return crawlMaxTime;
	}

	/**
	 * @return TODO: DOCUMENT ME!
	 */
	public static int getCrawlMaxStatesValue() {
		return crawlMaxStatesValue;
	}

	/**
	 * @return the max value for crawling time.
	 */
	public static int getCrawlMaxTimeValue() {
		return crawlMaxTimeValue;
	}

	/**
	 * Parses the tag elements.
	 * 
	 * @param text
	 *            The string containing the tag elements.
	 * @return The tag element.
	 */
	public static TagElement parseTagElements(String text) {
		if (text.equals("")) {
			return null;
		}
		TagElement tagElement = new TagElement();
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
				tagElement.setName(matcher.group().trim());
			}

			matcher = patternAttributes.matcher(substring);

			// attributes
			if (matcher.find()) {
				String attributes = (matcher.group());
				// parse attributes
				matcher = patternAttribute.matcher(attributes);

				while (matcher.find()) {
					String name = matcher.group(1).trim();
					String value = matcher.group(2).trim();
					tagElement.getAttributes().add(new TagAttribute(name, value));
				}
			}

			// id
			matcher = patternId.matcher(substring);
			if (matcher.find()) {
				String id = matcher.group(2);
				tagElement.setId(id);
			}

		}
		return tagElement;
	}

	/**
	 * @param args
	 *            TODO: DOCUMENT ME!
	 */
	public static void main(String[] args) {
		String text = "div:{class=expandable-hitarea}";

		TagElement tagElement = parseTagElements(text);
		System.out.println("tagname: " + tagElement.getName());

		for (TagAttribute attr : tagElement.getAttributes()) {
			System.out.println("attrName: " + attr.getName() + " value: " + attr.getValue());
		}

		/*
		 * String text =
		 * "a:{attr=value}, div:{class=aha; id=room}, span:{}, div:{class=expandable-hitarea}" ; try
		 * { PropertyHelper.init("src/test/resources/testcrawljax.properties"); } catch
		 * (ConfigurationException e) { System.out.println(e.getMessage()); } List<String> tList =
		 * getPropertyAsList(crawlTags); for (String e : tList) { System.out.println(e); TagElement
		 * tagElement = parseTagElements(e); System.out.println("tagname: " + tagElement.getName());
		 * for (TagAttribute attr : tagElement.getAttributes()) { System.out.println("attrName: " +
		 * attr.getName() + " value: " + attr.getValue()); } }
		 */

		/*
		 * for (String t : getPropertyAsList(crawlTags)) { TagElement tagElement =
		 * parseTagElements(t); if (tagElement != null) { crawlTagElements.add(tagElement); } }
		 */

		/*
		 * TagElement tagElement = parseTagElements(text); System.out.println("tagname: " +
		 * tagElement.getName()); for (TagAttribute attr : tagElement.getAttributes()) {
		 * System.out.println( "attrName: " + attr.getName() + " value: " + attr.getValue()); }
		 */
	}

	/**
	 * @return TODO: DOCUMENT ME!
	 */
	public static List<TagElement> getCrawlTagElements() {
		return crawlTagElements;
	}

	/**
	 * @return TODO: DOCUMENT ME!
	 */
	public static List<TagElement> getCrawlExcludeTagElements() {
		return crawlExcludeTagElements;
	}

	/**
	 * @return The hibernate properties filename.
	 */
	public static String getHibernatePropertiesValue() {
		return hibernatePropertiesValue;
	}

	/**
	 * @return the crawlManualEnterFormValue
	 */
	public static boolean getCrawlManualEnterFormValue() {
		return crawlManualEnterFormValue == 1;
	}

	/**
	 * @return The form properties.
	 */
	public static String getFormPropertiesValue() {
		return formPropertiesValue;
	}

	/**
	 * @return the crawlFormRandomInputValue
	 */
	public static boolean getCrawlFormWithRandomValues() {
		return crawlFormRandomInputValue == 1;
	}

	/**
	 * @return TODO: DOCUMENT ME!
	 */
	public static List<String> getAtusaPluginsValues() {
		return atusaPluginsValues;
	}

	/**
	 * @return Whether to use the proxy.
	 */
	public static boolean getProxyEnabledValue() {
		return proxyEnabledValue == 1;
	}

	/**
	 * @return the useDatabaseValue
	 */
	public static boolean useDatabase() {
		return useDatabaseValue == 1;
	}

	/**
	 * @return Whether to tidy up the dom.
	 */
	public static boolean getDomTidyValue() {
		return domTidyValue == 1;
	}

	/**
	 * Return the max history size.
	 * 
	 * @return Maximum history size.
	 */
	public static int getMaxHistorySize() {
		return maxHistorySize;
	}

	/**
	 * Returns the hibernate schema name.
	 * 
	 * @return The name.
	 */
	public static String getHibernateSchemaValue() {
		return hibernateSchemaValue;
	}

	/**
	 * @return the testInvariantsWhileCrawlingValue
	 */
	public static boolean getTestInvariantsWhileCrawlingValue() {
		return testInvariantsWhileCrawlingValue == 1;
	}

	/**
	 * @return the detectEventHandlers
	 */
	public static String getDetectEventHandlers() {
		return detectEventHandlers;
	}

	/**
	 * @return the detectEventHandlersValue
	 */
	public static boolean getDetectEventHandlersValue() {
		return detectEventHandlersValue == 1;
	}

	/**
	 * Get filename of the properties file in use.
	 * 
	 * @return Filename.
	 */
	public static String getPropertiesFileName() {
		return propertiesFileName;
	}

	/**
	 * @return the crawNumberOfThreadsValue
	 */
	public static int getCrawNumberOfThreadsValue() {
		return crawNumberOfThreadsValue;
	}

	/**
	 * @return if each candidate clickable should be clicked only once.
	 */
	public static boolean getClickOnceValue() {
		return clickOnceValue == 1;
	}
}
