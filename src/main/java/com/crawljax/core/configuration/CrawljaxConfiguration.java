package com.crawljax.core.configuration;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.openqa.selenium.WebDriver;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.browser.WebDriverFirefox;
import com.crawljax.browser.WebDriverOther;
import com.crawljax.condition.eventablecondition.EventableCondition;
import com.crawljax.core.plugin.Plugin;
import com.crawljax.util.Helper;
import com.crawljax.util.PropertyHelper;

/**
 * Specifies the settings Crawljax. The methods in this class fall into two categories:Oz
 * <p/>
 * <ul>
 * <li>General properties of Crawljax</li>
 * <li>Properties for the crawling
 * {@link CrawljaxConfiguration#setCrawlSpecification(CrawlSpecification)}</li>
 * </ul>
 * <p/>
 * By default Crawljax uses no database, but this can be enabled via
 * {@link CrawljaxConfiguration#setHibernateConfiguration(HibernateConfiguration)} See also
 * {@link HibernateConfiguration}
 * <p/>
 * DEFAULT VAlUES: Browser: WebDriverFirefox Project Full Path: empty Project Relative Path: empty
 * Filter attributes: closure_hashcode_(\\w)*, jquery[0-9]+ Test invariants while crawling: true
 * EXAMPLE: CrawljaxConfiguration crawljaxConfig = new CrawljaxConfiguration(); CrawlSpecification
 * crawler = new CrawlSpecification("http://www.google.com"); crawler.click("a");
 * crawljaxConfig.setCrawlSpecification(crawler);
 * 
 * @version $Id$
 */
public final class CrawljaxConfiguration {

	private static final int ONE_SECOND = 1000;

	private EmbeddedBrowser browser;

	private String outputFolder = "";
	private String projectRelativePath = "";

	private boolean useDatabase = false;

	private List<String> filterAttributeNames = new ArrayList<String>();

	private List<Plugin> plugins = new ArrayList<Plugin>();

	private CrawlSpecification crawlSpecification = new CrawlSpecification("");
	private HibernateConfiguration hibernateConfiguration = new HibernateConfiguration();
	private ProxyConfiguration proxyConfiguration = null;

	/**
	 * Constructor.
	 */
	public CrawljaxConfiguration() {
		addFilterAttribute("closure_hashcode_(\\w)*");
		addFilterAttribute("jquery[0-9]+");
	}

	/**
	 * @return The crawlSpecification which contains all the crawl settings.
	 */
	protected CrawlSpecification getCrawlSpecification() {
		return crawlSpecification;
	}

	/**
	 * @param crawlSpecification
	 *            Which contains all the crawl settings.
	 */
	public void setCrawlSpecification(CrawlSpecification crawlSpecification) {
		this.crawlSpecification = crawlSpecification;
	}

	/**
	 * @return The inputSpecification which contains information the data for setting input fields.
	 */
	protected InputSpecification getInputSpecification() {
		if (crawlSpecification != null) {
			return crawlSpecification.getInputSpecification();
		} else {
			return null;
		}
	}

	/**
	 * @return A PropertiesConfiguration. For use by PropertyHelper only!
	 */
	protected Configuration getConfiguration() {
		if (getCrawlSpecification() == null) {
			return null;
		}
		Configuration config = new PropertiesConfiguration();
		config.addProperty("output.path", getOutputFolder());
		config.addProperty("project.path.relative", getProjectRelativePath());

		config.addProperty("hibernate.hbm2ddl.auto", getHibernateConfiguration()
		        .getDatabaseScheme());
		config.addProperty("invariantcontroller.testcrawling", ConfigurationHelper
		        .booleanToInt(getCrawlSpecification().getTestInvariantsWhileCrawling()));

		// CrawlSpecification
		config.addProperty("site.url", getCrawlSpecification().getUrl());
		config.addProperty("database.use", getUseDatabaseAsInt());

		config.addProperty("click.once", ConfigurationHelper.booleanToInt(getCrawlSpecification()
		        .getClickOnce()));

		config.addProperty("robot.events", ConfigurationHelper
		        .listToString(getCrawlSpecification().getCrawlEvents()));
		config.addProperty("crawl.tags", ConfigurationHelper
		        .listToString(getAllIncludedCrawlElements()));
		config.addProperty("crawl.tags.exclude", ConfigurationHelper
		        .listToString(getCrawlSpecification().crawlActions().getCrawlElementsExcluded()));
		config.addProperty("crawl.filter.attributes", ConfigurationHelper
		        .listToString(getFilterAttributeNames()));
		config.addProperty("crawl.depth", getCrawlSpecification().getDepth());
		config.addProperty("crawl.wait.reload", getCrawlSpecification()
		        .getWaitTimeAfterReloadUrl());
		config.addProperty("crawl.wait.event", getCrawlSpecification().getWaitTimeAfterEvent());
		config.addProperty("crawl.max.states", getCrawlSpecification().getMaximumStates());
		config.addProperty("crawl.max.runtime", getCrawlSpecification().getMaximumRuntime()
		        * ONE_SECOND);
		config.addProperty("crawl.forms.randominput", ConfigurationHelper
		        .booleanToInt(getCrawlSpecification().getRandomInputInForms()));
		config.addProperty("crawl.numberOfThreads", getCrawlSpecification().getNumberOfThreads());

		if (getProxyConfiguration() != null) {
			config.addProperty("proxy.enabled", 1);
			config.addProperty("proxy.port", getProxyConfiguration().getPort());
		}

		return config;
	}

	/**
	 * Enable the crawljax proxy extension.
	 * 
	 * @param proxyConfiguration
	 *            The ProxyConfiguration to set.
	 */
	public void setProxyConfiguration(ProxyConfiguration proxyConfiguration) {
		this.proxyConfiguration = proxyConfiguration;
	}

	/**
	 * @return The proxyConfiguration to use.
	 */
	protected ProxyConfiguration getProxyConfiguration() {
		return proxyConfiguration;
	}

	/**
	 * @return All the included crawlTags.
	 */
	protected List<CrawlElement> getAllIncludedCrawlElements() {
		// first add elements for forms so that form action crawlTags are only
		// clicked
		// and not by another random crawlTag
		List<CrawlElement> crawlTags = getInputSpecification().getCrawlElements();
		if (getCrawlSpecification() != null) {
			for (CrawlElement crawlTag : getCrawlSpecification().crawlActions()
			        .getCrawlElements()) {
				crawlTags.add(crawlTag);
			}
		}
		return crawlTags;
	}

	/**
	 * @return All the added crawlTags.
	 */
	protected List<CrawlElement> getAllCrawlElements() {
		List<CrawlElement> crawlTags = getAllIncludedCrawlElements();
		if (getCrawlSpecification() != null) {
			for (CrawlElement crawlTag : getCrawlSpecification().crawlActions()
			        .getCrawlElementsExcluded()) {
				crawlTags.add(crawlTag);
			}
		}
		return crawlTags;
	}

	/**
	 * @return The eventableConditions.
	 */
	protected List<EventableCondition> getEventableConditions() {
		List<EventableCondition> eventableConditions = new ArrayList<EventableCondition>();
		for (CrawlElement crawlTag : getAllCrawlElements()) {
			EventableCondition eventableCondition = crawlTag.getEventableCondition();
			if (eventableCondition != null) {
				eventableConditions.add(eventableCondition);
			}
		}
		return eventableConditions;
	}

	/**
	 * @return The HibernateConfiguration which contains the Hibernate Database settings.
	 */
	protected HibernateConfiguration getHibernateConfiguration() {
		return hibernateConfiguration;
	}

	/**
	 * @param hibernateConfiguration
	 *            Which contains the Hibernate Database settings.
	 */
	public void setHibernateConfiguration(HibernateConfiguration hibernateConfiguration) {
		this.useDatabase = true;
		this.hibernateConfiguration = hibernateConfiguration;
	}

	/**
	 * @return The browser used to crawl. See {@link EmbeddedBrowser}. By default
	 *         {@link WebDriverFirefox} is used.
	 */
	protected EmbeddedBrowser getBrowser() {
		if (browser == null) {
			if (PropertyHelper.getCrawljaxConfiguration() != null
			        && PropertyHelper.getCrawljaxConfiguration().getProxyConfiguration() != null) {
				browser =
				        new WebDriverFirefox(PropertyHelper.getCrawljaxConfiguration()
				                .getProxyConfiguration());
			} else {
				browser = new WebDriverFirefox();
			}
		}
		return browser;
	}

	/**
	 * @param browser
	 *            The browser used to crawl. See {@link EmbeddedBrowser}. By default
	 *            {@link WebDriverFirefox} is used.
	 */
	public void setBrowser(EmbeddedBrowser browser) {
		this.browser = browser;
	}

	/**
	 * Deprecated function to specify the browser used. Replaced by
	 * {@link CrawljaxConfiguration#setBrowser(EmbeddedBrowser)}.
	 * 
	 * @see #setBrowser(EmbeddedBrowser)
	 * @param driver
	 *            The Webdriver driver used to crawl. By default {@link WebDriverFirefox} is used.
	 */
	@Deprecated
	public void setBrowser(WebDriver driver) {
		this.browser = new WebDriverOther(driver);
	}

	/**
	 * @return The path of the outputFolder with a trailing slash.
	 */
	protected String getOutputFolder() {
		return Helper.addFolderSlashIfNeeded(outputFolder);
	}

	/**
	 * @param path
	 *            The (absolute) path of the output folder.
	 */
	public void setOutputFolder(String path) {
		this.outputFolder = Helper.addFolderSlashIfNeeded(path);
	}

	/**
	 * @return The relative path of the project.
	 */
	protected String getProjectRelativePath() {
		return projectRelativePath;
	}

	/**
	 * @param projectRelativePath
	 *            The relative path of the project.
	 */
	public void setProjectRelativePath(String projectRelativePath) {
		this.projectRelativePath = projectRelativePath;
	}

	/**
	 * @return Whether a database is used.
	 */
	protected boolean getUseDatabase() {
		return useDatabase;
	}

	/**
	 * @return Whether a database is used as an integer.
	 */
	protected Integer getUseDatabaseAsInt() {
		if (useDatabase) {
			return 1;
		} else {
			return 0;
		}
	}

	/**
	 * @return The attributes which are filtered before the DOM is used.
	 */
	protected List<String> getFilterAttributeNames() {
		return filterAttributeNames;
	}

	/**
	 * @param filterAttributeNames
	 *            The attributes which are filtered before the DOM is used.
	 */
	public void setFilterAttributeNames(List<String> filterAttributeNames) {
		this.filterAttributeNames = filterAttributeNames;
	}

	/**
	 * Sets filter attribute names.
	 * 
	 * @param filterAttributeNames
	 *            The attribute names to filter.
	 */
	public void setFilterAttributeNames(String... filterAttributeNames) {
		for (String name : filterAttributeNames) {
			this.filterAttributeNames.add(name);
		}
	}

	/**
	 * @param attributeName
	 *            The name of the attributes which should be filtered before the DOM is used.
	 */
	public void addFilterAttribute(String attributeName) {
		this.filterAttributeNames.add(attributeName);
	}

	/**
	 * @return The plugins See {@link Plugin}.
	 */
	protected List<Plugin> getPlugins() {
		return plugins;
	}

	/**
	 * @param plugins
	 *            The plugins to set. See {@link Plugin}.
	 */
	public void setPlugins(List<Plugin> plugins) {
		this.plugins = plugins;
	}

	/**
	 * Add a plugin to the execution. Note that the order of adding is the same as running for the
	 * same type of plugin. This means that if you add a precrawling plugin p1 and next you add a
	 * precrawling plugin p2, p1 will be executed before p2.
	 * 
	 * @param plugin
	 *            Add a plugin. See {@link Plugin}.
	 */
	public void addPlugin(Plugin plugin) {
		this.plugins.add(plugin);
	}

}
