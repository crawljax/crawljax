package com.crawljax.core.configuration;

import java.util.ArrayList;
import java.util.List;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.browser.EmbeddedBrowserBuilder;
import com.crawljax.browser.WebDriverBrowserBuilder;
import com.crawljax.condition.eventablecondition.EventableCondition;
import com.crawljax.core.plugin.Plugin;
import com.crawljax.util.Helper;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

/**
 * Specifies the settings Crawljax. The methods in this class fall into two categories:Oz
 * <p/>
 * <ul>
 * <li>General properties of Crawljax</li>
 * <li>Properties for the crawling
 * {@link CrawljaxConfiguration#setCrawlSpecification(CrawlSpecification)}</li>
 * </ul>
 * DEFAULT VAlUES: Browser: webdriver firefox Project Full Path: empty Project Relative Path: empty
 * Filter attributes: closure_hashcode_(\\w)*, jquery[0-9]+ Test invariants while crawling: true
 * EXAMPLE: CrawljaxConfiguration crawljaxConfig = new CrawljaxConfiguration(); CrawlSpecification
 * crawler = new CrawlSpecification("http://www.google.com"); crawler.click("a");
 * crawljaxConfig.setCrawlSpecification(crawler);
 */
public final class CrawljaxConfiguration {

	private BrowserType browser = BrowserType.firefox;

	private EmbeddedBrowserBuilder browserBuilder = new WebDriverBrowserBuilder();

	private String remoteHubUrl = "";

	private String outputFolder = "";
	private String projectRelativePath = "";

	private List<String> filterAttributeNames = new ArrayList<String>();

	private List<Plugin> plugins = new ArrayList<Plugin>();

	private CrawlSpecification crawlSpecification = new CrawlSpecification("");
	private ProxyConfiguration proxyConfiguration = null;
	private ThreadConfiguration threadConfiguration = new ThreadConfiguration();

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
		Preconditions.checkNotNull(crawlSpecification);
		this.crawlSpecification = crawlSpecification;
	}

	/**
	 * @return The inputSpecification which contains information the data for setting input fields.
	 */
	protected InputSpecification getInputSpecification() {
		return crawlSpecification.getInputSpecification();
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
	 * @param threadConfiguration
	 *            the threadConfiguration to set
	 */
	public void setThreadConfiguration(ThreadConfiguration threadConfiguration) {
		this.threadConfiguration = threadConfiguration;
	}

	/**
	 * @return the threadConfiguration
	 */
	protected ThreadConfiguration getThreadConfiguration() {
		return threadConfiguration;
	}

	/**
	 * @return All the included crawlTags.
	 */
	protected ImmutableList<CrawlElement> getAllIncludedCrawlElements() {
		// first add elements for forms so that form action crawlTags are only
		// clicked and not by another random crawlTag
		return ImmutableList.<CrawlElement> builder()
		        .addAll(getInputSpecification().getCrawlElements())
		        .addAll(crawlSpecification.crawlActions().getCrawlElements())
		        .build();
	}

	/**
	 * @return All the added crawlTags.
	 */
	protected List<CrawlElement> getAllCrawlElements() {
		return ImmutableList.<CrawlElement> builder()
		        .addAll(getInputSpecification().getCrawlElements())
		        .addAll(crawlSpecification.crawlActions().getCrawlElements())
		        .addAll(getCrawlSpecification().crawlActions()
		                .getCrawlElementsExcluded())
		        .build();
	}

	/**
	 * @return The eventableConditions.
	 */
	protected ImmutableList<EventableCondition> getEventableConditions() {
		Builder<EventableCondition> eventableConditions = ImmutableList.builder();
		for (CrawlElement crawlTag : getAllCrawlElements()) {
			EventableCondition eventableCondition = crawlTag.getEventableCondition();
			if (eventableCondition != null) {
				eventableConditions.add(eventableCondition);
			}
		}

		return eventableConditions.build();
	}

	/**
	 * @return The browser used to crawl. By default firefox is used.
	 */
	protected BrowserType getBrowser() {
		return browser;
	}

	/**
	 * @param browser
	 *            The browser used to crawl.
	 */
	public void setBrowser(BrowserType browser) {
		this.browser = browser;
	}

	/**
	 * @return the browserBuilder
	 */
	protected EmbeddedBrowserBuilder getBrowserBuilder() {
		return browserBuilder;
	}

	/**
	 * Set the remote hub url that needs to be taken when using remote crawling.
	 * 
	 * @param remoteHubUrl
	 *            the url of the remote hub
	 */
	public void setRemoteHubUrl(String remoteHubUrl) {
		this.remoteHubUrl = remoteHubUrl;
	}

	/**
	 * @return the remoteHubUrl
	 */
	protected String getRemoteHubUrl() {
		return remoteHubUrl;
	}

	/**
	 * @param browserBuilder
	 *            the browserBuilder to set
	 */
	public void setBrowserBuilder(EmbeddedBrowserBuilder browserBuilder) {
		this.browserBuilder = browserBuilder;
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
