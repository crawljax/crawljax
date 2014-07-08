package com.crawljax.core.configuration;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.core.Crawler;
import com.crawljax.core.CrawljaxException;
import com.crawljax.core.configuration.CrawlRules.CrawlRulesBuilder;
import com.crawljax.core.plugin.Plugin;
import com.crawljax.core.state.StateVertexFactory;
import com.crawljax.core.state.duplicatedetection.FeatureType;
import com.crawljax.domcomparators.DomStructureStripper;
import com.crawljax.domcomparators.AttributesStripper;
import com.crawljax.domcomparators.DomStripper;
import com.crawljax.domcomparators.HeadStripper;
import com.crawljax.domcomparators.RedundantWhiteSpaceStripper;
import com.crawljax.domcomparators.ValidDomStripper;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * Configures the {@link Crawler}. Set it up using the {@link #builderFor(String)} function.
 */
public class CrawljaxConfiguration {

	public static class CrawljaxConfigurationBuilder {

		private final int LENGTH_OF_DUPLICATE_DETECTION_HASH = 32;
		
		private final ImmutableList.Builder<Plugin> pluginBuilder = ImmutableList.builder();
		private final ImmutableList.Builder<ValidDomStripper> validStrippers = ImmutableList.builder();
		private final ImmutableList.Builder<DomStripper> strippers = ImmutableList.builder();
		private final CrawljaxConfiguration config;
		private final CrawlRulesBuilder crawlRules;

		private CrawljaxConfigurationBuilder(URI url) {
			Preconditions.checkNotNull(url);
			config = new CrawljaxConfiguration();
			config.url = url;
			crawlRules = CrawlRules.builder(this);
		}

		/**
		 * If the website uses <a href="http://en.wikipedia.org/wiki/Basic_access_authentication">Basic auth</a> you
		 * can
		 * set the username and password here.
		 *
		 * @param username The username for the website.
		 * @param password The password for the website.
		 * @return {@link CrawljaxConfigurationBuilder} for method chaining.
		 */
		public CrawljaxConfigurationBuilder setBasicAuth(String username, String password) {
			try {
				String encodedUsername = URLEncoder.encode(username, "UTF-8");
				String encodedPassword = URLEncoder.encode(password, "UTF-8");
				String hostPrefix = encodedUsername + ":" + encodedPassword + "@";
				config.url = URI.create(config.url.toString().replaceFirst("://", "://" + hostPrefix));
			}
			catch (UnsupportedEncodingException e) {
				throw new CrawljaxException("Could not parse the username/password to a URL", e);
			}
			return this;
		}

		/**
		 * @param states The maximum number of states the Crawler should crawl. The default is unlimited.
		 */
		public CrawljaxConfigurationBuilder setMaximumStates(int states) {
			checkArgument(states > 1, "Number of maximum states should be larger than 1");
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
		 * @param time The maximum time the crawler should run. Default is one hour.
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
		 * @param depth The maximum depth the crawler can reach. The default is <code>2</code>.
		 */
		public CrawljaxConfigurationBuilder setMaximumDepth(int depth) {
			Preconditions.checkArgument(depth >= 0,
					"Depth should be 0 for infinite, or larger for a certain depth.");
			config.maximumDepth = depth;
			return this;
		}

		/**
		 * Set the crawl depth to unlimited. The default depth is <code>2</code>.
		 */
		public CrawljaxConfigurationBuilder setUnlimitedCrawlDepth() {
			config.maximumDepth = 0;
			return this;
		}

		/**
		 * @param threshold The threshold that will be used for the Hamming distance comparison.
		 * Two states are near-duplicates if the Hamming distance between the two hashes 
		 * are less or equal than the threshold.
		 */
		public CrawljaxConfigurationBuilder setThresholdNearDuplicateDetection(double threshold) {
			Preconditions.checkArgument(threshold >= 0,
					"The theshold should be greater or equal to 0.");
			Preconditions.checkArgument(threshold <= LENGTH_OF_DUPLICATE_DETECTION_HASH,
					"The theshold should be smaller or equal to " + 32);
			config.thresholdNearDuplicateDetection = threshold;
			return this;
		}
		
		/**
		 * @param features The features that will be used to in the Near-Duplicate Detection algorithm.
		 * Features determine how the relevant (stripped) content of a page is processed into a hash.  
		 */
		public CrawljaxConfigurationBuilder setFeaturesNearDuplicateDetection(List<FeatureType> features) {
			config.featuresNearDuplicateDetection = features;
			return this;
		}
		
		/**
		 * Add plugins to Crawljax. Note that without plugins, Crawljax won't give any ouput. For basic output at least
		 * enable the CrawlOverviewPlugin. <p> You can call this method several times to add multiple plugins </p>
		 *
		 * @param plugins the plugins you would like to enable.
		 */
		public CrawljaxConfigurationBuilder addPlugin(Plugin... plugins) {
			pluginBuilder.add(plugins);
			return this;
		}

		/**
		 * @param configuration The proxy configuration. Default is {@link ProxyConfiguration#noProxy()}
		 */
		public CrawljaxConfigurationBuilder setProxyConfig(ProxyConfiguration configuration) {
			Preconditions.checkNotNull(configuration);
			config.proxyConfiguration = configuration;
			return this;
		}

		/**
		 * @return The {@link CrawlRulesBuilder} to define crawling rules.
		 */
		public CrawlRulesBuilder crawlRules() {
			return crawlRules;
		}

		/**
		 * @param configuration a custom {@link BrowserConfiguration}. The default is a single {@link
		 *                      BrowserType#FIREFOX} browser.
		 */
		public CrawljaxConfigurationBuilder setBrowserConfig(BrowserConfiguration configuration) {
			Preconditions.checkNotNull(configuration);
			config.browserConfig = configuration;
			return this;
		}

		/**
		 * Set a custom {@link com.crawljax.core.state.StateVertexFactory} to be able to use your own {@link
		 * com.crawljax.core.state.StateVertex} objects. This is useful when you want to have a custom comparator in
		 * the
		 * stateflowgraph which relies on the {@link Object#hashCode()} or {@link Object#equals(Object)} of the {@link
		 * com.crawljax.core.state.StateVertex}.
		 *
		 * @param vertexFactory The factory you want to use.
		 * @return The builder for method chaining.
		 */
		public CrawljaxConfigurationBuilder setStateVertexFactory(StateVertexFactory vertexFactory) {
			Preconditions.checkNotNull(vertexFactory);
			config.stateVertexFactory = vertexFactory;
			return this;
		}

		/**
		 * Set the output folder for any {@link Plugin} you might configure. Crawljax itself doesn't need an output
		 * folder but many plug-ins do.
		 *
		 * @param output The output folder. If it does not exist it will be created.
		 * @throws IllegalStateException if the specified file is not writable or exists but isn't a folder.
		 */
		public CrawljaxConfigurationBuilder setOutputDirectory(File output) {
			config.output = output;
			checkOutputDirWritable();
			return this;
		}

		/**
		 * Add a {@link com.crawljax.domcomparators.DomStripper}.
		 *
		 * <p>
		 * If no {@link com.crawljax.domcomparators.DomStripper} or
		 * {@link com.crawljax.domcomparators.ValidDomStripper} is added,
		 * Crawljax uses the build-in {@link com.crawljax.domcomparators.DomStructureStripper}
		 *  and {@link com.crawljax.domcomparators.AttributesStripper}</p>
		 *
		 * <p>
		 * Out-of-the-box available strippers are:
		 * <ul>
		 * <li>{@link com.crawljax.domcomparators.DomTextContentStripper}</li>
		 * <li>{@link com.crawljax.domcomparators.DomStructureStripper}</li>
		 * <li>{@link com.crawljax.domcomparators.DomTextContentStripper}</li>
		 * <li>{@link com.crawljax.domcomparators.WhiteSpaceStripper}</li>
		 * </ul>
		 * </p>
		 * <p/>
		 * @param stripper The stripper you want to add. Order matters. Duplicates are allowed.
		 * @return the builder for method chaining.
		 */
		public CrawljaxConfigurationBuilder addDomStripper(DomStripper stripper) {
			strippers.add(stripper);
			return this;
		}

		/**
		 * Add a {@link com.crawljax.domcomparators.ValidDomStripper}.
		 *
		 * <p>If no {@link com.crawljax.domcomparators.DomStripper} or {@link com.crawljax.domcomparators
		 * .ValidDomStripper} is added, Crawljax uses the build-in
		 * {@link com.crawljax.domcomparators.DomTextContentStripper}
		 * and {@link com.crawljax.domcomparators.AttributesStripper}</p>
		 *
		 * <p>
		 * Out-of-the-box available strippers are:
		 * <ul>
		 * <li>{@link com.crawljax.domcomparators.DomTextContentStripper}</li>
		 * <li>{@link com.crawljax.domcomparators.DomStructureStripper}</li>
		 * <li>{@link com.crawljax.domcomparators.ByCssSelectorStripper}</li>
		 * <li>{@link com.crawljax.domcomparators.WhiteSpaceStripper}</li>
		 * <li>{@link com.crawljax.domcomparators.AttributesStripper}</li>
		 * </ul>
		 * </p>
		 * <p/>
		 *
		 * @param stripper The stripper you want to add. Order matters. Duplicates are allowed.
		 * @return the builder for method chaining.
		 */
		public CrawljaxConfigurationBuilder addValidDomStripper(ValidDomStripper stripper) {
			validStrippers.add(stripper);
			return this;
		}

		private void checkOutputDirWritable() {
			if (!config.output.exists()) {
				Preconditions.checkState(config.output.mkdirs(),
						"Could not create the output directory %s ", config.output);
			}
			else {
				Preconditions.checkArgument(config.output.isDirectory(),
						"Output directory %s is not a folder", config.output);
				Preconditions.checkState(config.output.canWrite(),
						"Output directory %s is not writable", config.output);
			}
		}

		public CrawljaxConfiguration build() {
			config.plugins = pluginBuilder.build();
			config.crawlRules = crawlRules.build();
			config.strippers = strippers.build();
			config.validStrippers = validStrippers.build();

			if (config.strippers.isEmpty() && config.validStrippers.isEmpty()) {
				config.strippers = ImmutableList.of(
						new HeadStripper(),
						new DomStructureStripper(),
						new AttributesStripper(),
						new RedundantWhiteSpaceStripper()
				);
			}
			return config;
		}

	}

	/**
	 * @param url The url you want to setup a configuration for
	 * @return The builder to configure the crawler.
	 */
	public static CrawljaxConfigurationBuilder builderFor(URI url) {
		Preconditions.checkNotNull(url, "URL was null");
		return new CrawljaxConfigurationBuilder(url);
	}

	/**
	 * @param url The url you want to setup a configuration for
	 * @return The builder to configure the crawler.
	 */
	public static CrawljaxConfigurationBuilder builderFor(String url) {
		return new CrawljaxConfigurationBuilder(URI.create(url));
	}

	private URI url;

	private BrowserConfiguration browserConfig = new BrowserConfiguration(BrowserType.FIREFOX);
	private ImmutableList<Plugin> plugins;
	private ImmutableList<DomStripper> strippers;
	private ImmutableList<ValidDomStripper> validStrippers;
	private ProxyConfiguration proxyConfiguration = ProxyConfiguration.noProxy();

	private CrawlRules crawlRules;

	private int maximumStates = 0;
	private long maximumRuntime = TimeUnit.HOURS.toMillis(1);
	;
	private int maximumDepth = 2;
	private File output = new File("out");
	private double thresholdNearDuplicateDetection = 3;
	public List<FeatureType> featuresNearDuplicateDetection = new ArrayList<FeatureType>();

	private StateVertexFactory stateVertexFactory;

	private CrawljaxConfiguration() {
	}

	public URI getUrl() {
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

	public File getOutputDir() {
		return output;
	}
	
	public double getThresholdNearDuplicateDetection() {
		return thresholdNearDuplicateDetection;
	}

	public List<FeatureType> getFeaturesNearDuplicateDetection() { 
		return featuresNearDuplicateDetection;
	}
	
	public ImmutableList<DomStripper> getStrippers() {
		return strippers;
	}

	public ImmutableList<ValidDomStripper> getValidStrippers() {
		return validStrippers;
	}

	public StateVertexFactory getStateVertexFactory() {
		return stateVertexFactory;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(url, browserConfig, plugins, proxyConfiguration, crawlRules,
				maximumStates, maximumRuntime, maximumDepth, thresholdNearDuplicateDetection, strippers, validStrippers);
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof CrawljaxConfiguration) {
			CrawljaxConfiguration that = (CrawljaxConfiguration) object;
			return Objects.equal(this.url, that.url)
					&& Objects.equal(this.browserConfig, that.browserConfig)
					&& Objects.equal(this.plugins, that.plugins)
					&& Objects.equal(this.proxyConfiguration, that.proxyConfiguration)
					&& Objects.equal(this.crawlRules, that.crawlRules)
					&& Objects.equal(this.maximumStates, that.maximumStates)
					&& Objects.equal(this.maximumRuntime, that.maximumRuntime)
					&& Objects.equal(this.maximumDepth, that.maximumDepth)
					&& Objects.equal(this.thresholdNearDuplicateDetection, that.thresholdNearDuplicateDetection)
					&& Objects.equal(this.strippers, that.strippers)
					&& Objects.equal(this.validStrippers, that.validStrippers);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("url", url)
				.add("browserConfig", browserConfig)
				.add("plugins", plugins)
				.add("proxyConfiguration", proxyConfiguration)
				.add("crawlRules", crawlRules)
				.add("maximumStates", maximumStates)
				.add("maximumRuntime", maximumRuntime)
				.add("maximumDepth", maximumDepth)
				.add("thresholdNearDuplicateDetection", thresholdNearDuplicateDetection)
				.toString();
	}

}