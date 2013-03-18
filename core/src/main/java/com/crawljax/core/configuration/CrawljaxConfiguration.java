package com.crawljax.core.configuration;

import static com.google.common.base.Preconditions.checkArgument;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.core.Crawler;
import com.crawljax.core.CrawljaxException;
import com.crawljax.core.configuration.CrawlRules.CrawlRulesBuilder;
import com.crawljax.core.plugin.Plugin;
import com.crawljax.core.plugin.Plugins;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * Configures the {@link Crawler}. Set it up using the {@link #builderFor(String)} function.
 */
public final class CrawljaxConfiguration {

	public static class CrawljaxConfigurationBuilder {

		private final ImmutableList.Builder<Plugin> pluginBuilder = ImmutableList.builder();
		private final CrawljaxConfiguration config;
		private final CrawlRulesBuilder crawlRules = CrawlRules.builder();

		private CrawljaxConfigurationBuilder(URL url) {
			Preconditions.checkNotNull(url);
			config = new CrawljaxConfiguration();
			config.url = url;
		}

		/**
		 * @param states
		 *            The maximum number of states the Crawler should crawl. The default is
		 *            unlimited.
		 */
		public CrawljaxConfigurationBuilder setMaximumStates(int states) {
			checkArgument(states > 1, "Number of maximum states should be largen than 1");
			config.maximumStates = states;
			return this;
		}

		/**
		 * Crawl without a maximum state limit.
		 */
		public CrawljaxConfigurationBuilder setUnlimitedStates() {
			config.maximumStates = 0;
			return this;
		}

		/**
		 * @param time
		 *            The maximum time the crawler should run. Default is one hour.
		 */
		public CrawljaxConfigurationBuilder setMaximumRunTime(long time, TimeUnit unit) {
			checkArgument(time >= 0, "Time should be larger than 0, or 0 for infinate.");
			config.maximumRuntime = unit.toMillis(time);
			return this;
		}

		/**
		 * Set the maximum runtime to unlimited.
		 */
		public CrawljaxConfigurationBuilder setUnlimitedRuntime() {
			config.maximumRuntime = 0;
			return this;
		}

		/**
		 * @param time
		 *            The maximum depth the crawler can reach. The default is <code>2</code>.
		 */
		public CrawljaxConfigurationBuilder setMaximumDepth(int depth) {
			Preconditions.checkArgument(depth >= 0,
			        "Depth should be 0 for infinite, or larger for a certain depth.");
			config.maximumDepth = depth;
			return this;
		}

		/**
		 * Set the crawl depth to unlimited.
		 */
		public CrawljaxConfigurationBuilder setUnlimitedCrawlDepth() {
			config.maximumDepth = 0;
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
			Preconditions.checkNotNull(configuration);
			config.proxyConfiguration = configuration;
			return this;
		}

		/**
		 * @return The {@link CrawlRulesBuilder} to define crawling rules. If no specified, Crawljax
		 *         will do {@link CrawlRulesBuilder#}
		 */
		public CrawlRulesBuilder crawlRules() {
			return crawlRules;
		}

		/**
		 * @param configuration
		 *            a custom {@link BrowserConfiguration}. The default is a single
		 *            {@link BrowserType#firefox} browser.
		 */
		public CrawljaxConfigurationBuilder setBrowserConfig(BrowserConfiguration configuration) {
			Preconditions.checkNotNull(configuration);
			config.browserConfig = configuration;
			return this;
		}

		public CrawljaxConfiguration build() {
			config.plugins = new Plugins(pluginBuilder.build());
			config.crawlRules = crawlRules.build();
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
	private Plugins plugins;
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

	public Plugins getPlugins() {
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((browserConfig == null) ? 0 : browserConfig.hashCode());
		result = prime * result + ((crawlRules == null) ? 0 : crawlRules.hashCode());
		result = prime * result + maximumDepth;
		result = prime * result + (int) (maximumRuntime ^ (maximumRuntime >>> 32));
		result = prime * result + maximumStates;
		result = prime * result + ((plugins == null) ? 0 : plugins.hashCode());
		result =
		        prime * result
		                + ((proxyConfiguration == null) ? 0 : proxyConfiguration.hashCode());
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		CrawljaxConfiguration other = (CrawljaxConfiguration) obj;
		if (browserConfig == null) {
			if (other.browserConfig != null) {
				return false;
			}
		} else if (!browserConfig.equals(other.browserConfig)) {
			return false;
		}
		if (crawlRules == null) {
			if (other.crawlRules != null) {
				return false;
			}
		} else if (!crawlRules.equals(other.crawlRules)) {
			return false;
		}
		if (maximumDepth != other.maximumDepth) {
			return false;
		}
		if (maximumRuntime != other.maximumRuntime) {
			return false;
		}
		if (maximumStates != other.maximumStates) {
			return false;
		}
		if (plugins == null) {
			if (other.plugins != null) {
				return false;
			}
		} else if (!plugins.equals(other.plugins)) {
			return false;
		}
		if (proxyConfiguration == null) {
			if (other.proxyConfiguration != null) {
				return false;
			}
		} else if (!proxyConfiguration.equals(other.proxyConfiguration)) {
			return false;
		}
		if (url == null) {
			if (other.url != null) {
				return false;
			}
		} else if (!url.equals(other.url)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CrawljaxConfiguration [url=");
		builder.append(url);
		builder.append(", browserConfig=");
		builder.append(browserConfig);
		builder.append(", plugins=");
		builder.append(plugins);
		builder.append(", proxyConfiguration=");
		builder.append(proxyConfiguration);
		builder.append(", crawlRules=");
		builder.append(crawlRules);
		builder.append(", maximumStates=");
		builder.append(maximumStates);
		builder.append(", maximumRuntime=");
		builder.append(maximumRuntime);
		builder.append(", maximumDepth=");
		builder.append(maximumDepth);
		builder.append("]");
		return builder.toString();
	}

}