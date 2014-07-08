package com.crawljax.cli;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import ch.qos.logback.classic.Level;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.core.CrawljaxException;
import com.crawljax.core.CrawljaxRunner;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.core.state.NDDStateVertexFactory;
import com.crawljax.core.state.duplicatedetection.FeatureShingles;
import com.crawljax.core.state.duplicatedetection.FeatureShingles.ShingleType;
import com.crawljax.core.state.duplicatedetection.FeatureType;
import com.crawljax.core.state.duplicatedetection.NearDuplicateDetectionBroder;
import com.crawljax.core.state.duplicatedetection.NearDuplicateDetectionCrawlhash;
import com.crawljax.domcomparators.AttributesStripper;
import com.crawljax.domcomparators.DomStructureStripper;
import com.crawljax.domcomparators.HeadStripper;
import com.crawljax.domcomparators.RedundantWhiteSpaceStripper;
import com.crawljax.plugins.crawloverview.CrawlOverview;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;
import com.google.common.io.Resources;

import org.apache.commons.cli.ParseException;

public class JarRunner {

	static final String MISSING_ARGUMENT_MESSAGE =
	        "Missing required argument URL and/or output folder.";

	private final ParameterInterpeter options;

	private final CrawljaxConfiguration config;

	/**
	 * Main executable method of Crawljax CLI.
	 * 
	 * @param args
	 *            the arguments.
	 */
	public static void main(String[] args) {
		try {
			JarRunner runner = new JarRunner(args);
			runner.runIfConfigured();
		} catch (NumberFormatException e) {
			System.err.println("Could not parse number " + e.getMessage());
			System.exit(1);
		} catch (RuntimeException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
	}

	@VisibleForTesting
	JarRunner(String args[]) {
		try {
			this.options = new ParameterInterpeter(args);
		} catch (ParseException e) {
			throw new IllegalArgumentException(e.getMessage(), e);
		}
		if (options.requestsVersion()) {
			System.out.println(getCrawljaxVersion());
			this.config = null;
		} else if (options.necessaryArgsProvided()) {
			String url = options.getUrl();
			String outputDir = options.getOutputDir();
			configureLogging();
			this.config = readConfig(url, outputDir);
		} else {
			if (!options.requestsHelp()) {
				System.out.println(MISSING_ARGUMENT_MESSAGE);
			}
			options.printHelp();
			this.config = null;
		}
	}

	private String getCrawljaxVersion() {
		try {
			return Resources
			        .toString(JarRunner.class.getResource("/project.version"), Charsets.UTF_8);
		} catch (IOException e) {
			throw new CrawljaxException(e.getMessage(), e);
		}
	}

	private void configureLogging() {
		if (options.requestsVerbosity()) {
			LogUtil.setCrawljaxLogLevel(Level.INFO);
		}
		if (options.specifiesLogFile()) {
			File f = new File(options.getSpecifiedLogFile());
			try {
				if (!f.exists()) {
					Files.createParentDirs(f);
					Files.touch(f);
				}
			} catch (IOException e) {
				throw new CrawljaxException("Could not create log file: " + e.getMessage(), e);
			}
			Preconditions.checkArgument(f.canWrite());
			LogUtil.logToFile(f.getPath());
		}

	}

	private CrawljaxConfiguration readConfig(String urlValue, String outputDir) {
		CrawljaxConfigurationBuilder builder = CrawljaxConfiguration.builderFor(urlValue);

		builder.setOutputDirectory(new File(outputDir));

		BrowserType browser = BrowserType.FIREFOX;
		if (options.specifiesBrowser()) {
			browser = options.getSpecifiedBrowser();
		}

		int browsers = 1;
		if (options.specifiesParallelBrowsers()) {
			browsers = options.getSpecifiedNumberOfBrowsers();
		}
		if (browser == BrowserType.REMOTE) {
			String remoteUrl = options.getSpecifiedRemoteBrowser();
			builder.setBrowserConfig(BrowserConfiguration.remoteConfig(browsers, remoteUrl));
		} else {
			builder.setBrowserConfig(new BrowserConfiguration(browser, browsers));
		}

		if (options.specifiesDepth()) {
			builder.setMaximumDepth(options.getSpecifiedDepth());
		}

		if (options.specifiesMaxStates()) {
			builder.setMaximumStates(options.getMaxStates());
		}

		if (options.requestsCrawlHiddenAnchors()) {
			builder.crawlRules().crawlHiddenAnchors(true);
		}

		configureDuplicateDetection(builder);
		configureTimers(builder);

		builder.addPlugin(new CrawlOverview());

		if (options.specifiesClickElements()) {
			builder.crawlRules().click(options.getSpecifiedClickElements());
		} else {
			builder.crawlRules().clickDefaultElements();
		}

		return builder.build();
	}

	private void configureDuplicateDetection(CrawljaxConfigurationBuilder builder) {
		if (!options.specifiesUseBroder() && !options.specifiesUseCrawlhash()) {
			return;
		}
		// Use NDD-StateVertexFactory, if no ndd is provided a default will be used.
		builder.setStateVertexFactory(new NDDStateVertexFactory());

		// Build feature
		int featureSize = options.specifiesFeatureSize() ? options.getSpecifiedFeatureSize() : 3;
		ShingleType featureType =
		        options.specifiesFeatureType() ? options.getSpecifiedFeatureType()
		                : ShingleType.WORDS;
		List<FeatureType> features = new ArrayList<FeatureType>(1);
		if (featureType.equals(ShingleType.REGEX)) {
			features.add(FeatureShingles.withSize(featureSize, options.getSpecifiedFeatureRegEx()));
		} else {
			features.add(FeatureShingles.withSize(featureSize, featureType));
		}

		// If custom settings are provided use those, otherwise use presets
		if (options.specifiesUseBroder()) {
			double threshold =
			        options.specifiesThreshold() ? options.getSpecifiedThreshold() : 0.2D;
			builder.setNearDuplicateDetectionFactory(
			        new NearDuplicateDetectionBroder(threshold, ImmutableList.copyOf(features)));
		} else if (options.specifiesUseCrawlhash()) {
			double threshold =
			        options.specifiesThreshold() ? options.getSpecifiedThreshold() : 3D;
			builder.setNearDuplicateDetectionFactory(
			        new NearDuplicateDetectionCrawlhash(threshold, ImmutableList.copyOf(features)));
		}
		
		// Use default strippers of ndd		
		builder.addDomStripper(new HeadStripper());
		builder.addDomStripper(new DomStructureStripper());
		builder.addDomStripper(new AttributesStripper());
		builder.addDomStripper(new RedundantWhiteSpaceStripper());
	}

	private void configureTimers(CrawljaxConfigurationBuilder builder) {
		if (options.specifiesTimeOut()) {
			builder.setMaximumRunTime(options.getSpecifiedTimeOut(), TimeUnit.MINUTES);
		}
		if (options.specifiesWaitAfterEvent()) {
			builder.crawlRules().waitAfterEvent(options.getSpecifiedWaitAfterEvent(),
			        TimeUnit.MILLISECONDS);
		}
		if (options.specifiesWaitAfterReload()) {
			builder.crawlRules().waitAfterReloadUrl(options.getSpecifiedWaitAfterReload(),
			        TimeUnit.MILLISECONDS);
		}
	}

	private void runIfConfigured() {
		if (config != null) {
			CrawljaxRunner runner = new CrawljaxRunner(config);
			runner.call();
		}
	}

	@VisibleForTesting
	CrawljaxConfiguration getConfig() {
		return config;
	}
}
