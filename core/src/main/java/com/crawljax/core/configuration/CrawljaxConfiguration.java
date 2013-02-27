package com.crawljax.core.configuration;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.core.Crawler;
import com.crawljax.core.CrawljaxException;
import com.crawljax.core.plugin.Plugin;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * Configures the {@link Crawler}. Set it up using the {@link #builderFor(String)} function.
 */
public final class CrawljaxConfiguration {

	public static class CrawljaxConfigurationBuilder {

		private final ImmutableList.Builder<Plugin> pluginBuilder = ImmutableList.builder();
		private CrawljaxConfiguration config;

		private CrawljaxConfigurationBuilder(URL url) {
			config = new CrawljaxConfiguration();
			config.url = url;
		}

		/**
		 * @param states
		 *            The maximum number of states the Crawler should crawl. The default is
		 *            unlimited.
		 */
		public CrawljaxConfigurationBuilder setMaximumStates(int states) {
			Preconditions.checkArgument(states > 1, "States should be positive");
			config.maximumStates = states;
			return this;
		}

		/**
		 * @param time
		 *            The maximum time the crawler should run. Default is one hour.
		 */
		public CrawljaxConfigurationBuilder setMaximumRunTime(long time, TimeUnit unit) {
			Preconditions.checkArgument(time > 0, "Time should larger than 0");
			config.maximumRuntime = unit.toMillis(time);
			return this;
		}

		/**
		 * @param time
		 *            The maximum depth the crawler can reach. The default is <code>2</code>.
		 */
		public CrawljaxConfigurationBuilder setMaximumDepth(int depth) {
			Preconditions.checkArgument(depth > 1, "Time should larger than 1");
			config.maximumDepth = depth;
			return this;
		}

		/**
		 * Add plugins to Crawljax. Note that without plugins, Crawljax won't give any ouput. For
		 * basic output at least enable the CrawlOverviewPlugin.
		 * <p>
		 * You can call this method several times to add multiple plugins
		 * </p>
		 * 
		 * @param plugins
		 *            the plugins you would like to enable.
		 */
		public CrawljaxConfigurationBuilder addPlugin(Plugin... plugins) {
			pluginBuilder.add(plugins);
			return this;
		}

		/**
		 * @param configuration
		 *            The proxy configuration. Default is {@link ProxyConfiguration#noProxy()}
		 */
		public CrawljaxConfigurationBuilder setProxyConfig(ProxyConfiguration configuration) {
			config.proxyConfiguration = configuration;
			return this;
		}

		/**
		 * @param configuration
		 *            a custom {@link BrowserConfiguration}. The default is a single
		 *            {@link BrowserType#firefox} browser.
		 */
		public CrawljaxConfigurationBuilder setBrowserConfig(BrowserConfiguration configuration) {
			config.browserConfig = configuration;
			return this;
		}

		public CrawljaxConfiguration build() {
			config.plugins = pluginBuilder.build();
			return config;
		}

	}

	/**
	 * @param url
	 *            The url you want to setup a configuration for
	 * @return The builder to configure the crawler.
	 */
	public static CrawljaxConfigurationBuilder builderFor(URL url) {
		Preconditions.checkNotNull(url, "URL was null");
		return new CrawljaxConfigurationBuilder(url);
	}

	/**
	 * @param url
	 *            The url you want to setup a configuration for
	 * @return The builder to configure the crawler.
	 */
	public static CrawljaxConfigurationBuilder builderFor(String url) {
		try {
			return new CrawljaxConfigurationBuilder(new URL(url));
		} catch (MalformedURLException e) {
			throw new CrawljaxException("Could not read that URL", e);
		}
	}

	private URL url;

	private BrowserConfiguration browserConfig = new BrowserConfiguration(BrowserType.firefox);
	private ImmutableList<Plugin> plugins;
	private ProxyConfiguration proxyConfiguration = ProxyConfiguration.noProxy();

	private CrawlRules crawlRules;

	private int maximumStates = 0;
	private long maximumRuntime = TimeUnit.HOURS.toMillis(1);;
	private int maximumDepth = 2;

	private CrawljaxConfiguration() {
	}

	public URL getUrl() {
		return url;
	}

	public BrowserConfiguration getBrowserConfig() {
		return browserConfig;
	}

	public ImmutableList<Plugin> getPlugins() {
		return plugins;
	}

	public ProxyConfiguration getProxyConfiguration() {
		return proxyConfiguration;
	}

	public CrawlRules getCrawlRules() {
		return crawlRules;
	}

	public int getMaximumStates() {
		return maximumStates;
	}

	public long getMaximumRuntime() {
		return maximumRuntime;
	}

	public int getMaximumDepth() {
		return maximumDepth;
	}

}