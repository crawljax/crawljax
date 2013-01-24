package com.crawljax.core.configuration;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import com.crawljax.core.TagAttribute;
import com.crawljax.core.TagElement;
import com.google.common.collect.ImmutableSet;

/**
 * This class is used to create a CrawljaxConfiguration object configured with settings from a file.
 * 
 * @author Frank Groeneveld <frankgroeneveld+crawljax@gmail.com>
 * @version $Id$
 */
public class PropertiesFile {

	private final CrawljaxConfiguration config;

	private static final String PROJECTRELATIVEPATH = "project.path.relative";
	private static final String OUTPUTFOLDERNAME = "output.path";
	private static final String SITEURL = "site.url";
	private static final String CRAWLDEPTH = "crawl.depth";
	private static final String CRAWLMAXSTATES = "crawl.max.states";
	private static final String CRAWLMAXTIME = "crawl.max.runtime";

	private static final String CRAWLTAGS = "crawl.tags";
	private static final String CRAWLEXCLUDETAGS = "crawl.tags.exclude";
	private static final String CRAWLFILTERATTRIBUTES = "crawl.filter.attributes";

	private static final String CRAWLFORMRANDOMINPUT = "crawl.forms.randominput";

	/*
	 * TODO all, support this in the future? Or require to use API? private static final String
	 * HIBERNATEPROPERTIES = "hibernate.properties"; private static final String FORMPROPERTIES =
	 * "forms.properties"; private static final String HIBERNATESCHEMA = "hibernate.hbm2ddl.auto";
	 * private static final String USEDATABASE = "database.use";
	 */

	private static final String CRAWLWAITRELOAD = "crawl.wait.reload";
	private static final String CRAWLWAITEVENT = "crawl.wait.event";

	// if each candidate clickable should be clicked only once
	private static String clickOnce = "click.once";

	private static final String PROXYENABLED = "proxy.enabled";

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
	@SuppressWarnings("unchecked")
	private void read(PropertiesConfiguration file) {
		if (file.containsKey(OUTPUTFOLDERNAME)) {
			config.setOutputFolder(file.getString(OUTPUTFOLDERNAME));
		}
		config.setProjectRelativePath(file.getString(PROJECTRELATIVEPATH));

		config.setCrawlSpecification(getCrawlSpecification(file));

		config.setFilterAttributeNames(file.getList(CRAWLFILTERATTRIBUTES));

		if (file.containsKey(PROXYENABLED) && file.getBoolean(PROXYENABLED)) {
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

		crawler.setWaitTimeAfterEvent(file.getInt(CRAWLWAITEVENT));
		crawler.setWaitTimeAfterReloadUrl(file.getInt(CRAWLWAITRELOAD));

		crawler.setRandomInputInForms(file.getInt(CRAWLFORMRANDOMINPUT) == 1);

		setClickTags(file, crawler);

		return crawler;
	}

	/**
	 * Sets the click and ignore tags using the API.
	 * 
	 * @param file
	 *            Configuration file.
	 * @param crawler
	 *            Crawlspecification to set them on.
	 */
	@SuppressWarnings("unchecked")
	private void setClickTags(PropertiesConfiguration file, CrawlSpecification crawler) {
		/* set click tags */
		List<String> tags = file.getList(CRAWLTAGS);
		TagElement tagElement;

		/* walk through all elements */
		for (String tag : tags) {
			/* call the correct api stuff on the crawler for tag */
			tagElement = parseTagElement(tag);

			CrawlElement element = crawler.click(tagElement.getName());
			for (TagAttribute attrib : tagElement.getAttributes()) {
				element.withAttribute(attrib.getName(), attrib.getValue());
			}
		}

		/* do the same for the exclude tags */
		tags = file.getList(CRAWLEXCLUDETAGS);

		/* walk through all elements */
		for (String tag : tags) {
			/* call the correct api stuff on the crawler for tag */
			tagElement = parseTagElement(tag);

			CrawlElement element = crawler.dontClick(tagElement.getName());
			for (TagAttribute attrib : tagElement.getAttributes()) {
				element.withAttribute(attrib.getName(), attrib.getValue());
			}
		}
	}

	/**
	 * Parses the tag elements.
	 * 
	 * @param text
	 *            The string containing the tag elements.
	 * @return the TagElement;
	 */
	public TagElement parseTagElement(String text) {
		if (text.equals("")) {
			return null;
		}
		String name = null;

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

			ImmutableSet.Builder<TagAttribute> attributes = ImmutableSet.builder();
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

			TagElement el = new TagElement(attributes.build(), name, id);
			return el;
		}

		return null;
	}

	/**
	 * @return The configuration object that represents the file contents.
	 */
	public CrawljaxConfiguration getConfiguration() {
		return config;
	}
}
