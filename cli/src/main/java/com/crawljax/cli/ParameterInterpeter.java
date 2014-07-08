package com.crawljax.cli;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.validator.routines.UrlValidator;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.core.CrawljaxException;
import com.crawljax.core.configuration.CrawlRules;
import com.crawljax.core.state.duplicatedetection.FeatureShingles.ShingleType;
import com.google.common.base.Joiner;

class ParameterInterpeter {

	static final String HELP_MESSAGE =
	        "java -jar crawljax-cli-version.jar theUrl theOutputDir";

	private static final int SPACES_AFTER_OPTION = 3;
	private static final int SPACES_BEFORE_OPTION = 5;
	private static final int ROW_WIDTH = 80;

	static final String VERSION = "version";
	static final String VERBOSE = "verbose";
	static final String HELP = "help";
	static final String MAXSTATES = "maxstates";
	static final String DEPTH = "depth";
	static final String BROWSER = "browser";
	static final String BROWSER_REMOTE_URL = "browserRemoteUrl";
	static final String PARALLEL = "parallel";
	static final String OVERRIDE = "override";
	static final String CRAWL_HIDDEN_ANCHORS = "crawlHiddenAnchors";
	static final String TIME_OUT = "timeout";
	static final String WAIT_AFTER_RELOAD = "waitAfterReload";
	static final String WAIT_AFTER_EVENT = "waitAfterEvent";
	static final String LOG_FILE = "log";
	static final String CLICK = "click";
	static final String BRODER_DUPLICATE_DETECTION = "useBroder";
	static final String CRAWLHASH_DUPLICATE_DETECTION = "useCrawlhash";
	static final String THRESHOLD = "threshold";
	static final String FEATURE_TYPE = "featureType";
	static final String FEATURE_SIZE = "featureSize";
	

	private final Options options;
	private final CommandLine parameters;

	ParameterInterpeter(String args[]) throws ParseException {
		this.options = getOptions();
		this.parameters = new GnuParser().parse(options, args);
	}

	/**
	 * Create the CML Options.
	 * 
	 * @return Options expected from command-line.
	 */
	private Options getOptions() {
		Options options = new Options();
		options.addOption("h", HELP, false, "print this message");
		options.addOption(VERSION, false, "print the version information and exit");

		options.addOption("b", BROWSER, true,
		        "browser type: " + availableBrowsers() + ". Default is Firefox");

		options.addOption(BROWSER_REMOTE_URL, true,
		        "The remote url if you have configured a remote browser");

		options.addOption("d", DEPTH, true, "crawl depth level. Default is 2");

		options.addOption("s", MAXSTATES, true,
		        "max number of states to crawl. Default is 0 (unlimited)");

		options.addOption("p", PARALLEL, true,
		        "Number of browsers to use for crawling. Default is 1");
		options.addOption("o", OVERRIDE, false, "Override the output directory if non-empty");

		options.addOption("a", CRAWL_HIDDEN_ANCHORS, false,
		        "Crawl anchors even if they are not visible in the browser.");

		options.addOption("t", TIME_OUT, true,
		        "Specify the maximum crawl time in minutes");

		options.addOption(CLICK, true,
		        "a comma separated list of HTML tags that should be clicked. Default is A and BUTTON");

		options.addOption(WAIT_AFTER_EVENT, true,
		        "the time to wait after an event has been fired in milliseconds. Default is "
		                + CrawlRules.DEFAULT_WAIT_AFTER_EVENT);

		options.addOption(WAIT_AFTER_RELOAD, true,
		        "the time to wait after an URL has been loaded in milliseconds. Default is "
		                + CrawlRules.DEFAULT_WAIT_AFTER_RELOAD);

		options.addOption("v", VERBOSE, false, "Be extra verbose");
		
		options.addOption(LOG_FILE, true, "Log to this file instead of the console");
		
		//Duplicate Detection options		
		options.addOption(BRODER_DUPLICATE_DETECTION,  false, "Use the near-duplicate detection using Broder's algorithm.");
		options.addOption(CRAWLHASH_DUPLICATE_DETECTION,  false, "Use the near-duplicate detection using Broder's algorithm.");
		options.addOption(THRESHOLD,  true, "Set a threshold for Broder or Crawlhash near-duplicate detection.");
		options.addOption(FEATURE_SIZE,  true, "set the feature-size for a feature-type.");
		options.addOption(FEATURE_TYPE,  true, "set the feature-type used in Broder or Crawlhash near-duplicate detection.");

		return options;
	}

	private String availableBrowsers() {
		return Joiner.on(", ").join(BrowserType.values());
	}

	boolean requestsVersion() {
		return parameters.hasOption(VERSION);
	}

	boolean necessaryArgsProvided() {
		if (parameters.getArgs().length == 2) {
			checkUrlValidity(getUrl());
			checkOutDir(getOutputDir());
			whenRemoteBrowserNeedsUrl();
			return true;
		} else {
			return false;
		}
	}

	private void whenRemoteBrowserNeedsUrl() {
		if (specifiesBrowser() && getSpecifiedBrowser() == BrowserType.REMOTE) {
			checkArgument(!isNullOrEmpty(getSpecifiedRemoteBrowser()),
			        "When using remote browser the URL cannot be null");
		}

	}

	private void checkUrlValidity(String urlValue) {
		String[] schemes = { "http", "https" };
		if (urlValue == null || !new UrlValidator(schemes).isValid(urlValue)) {
			throw new IllegalArgumentException("provide a valid URL like http://example.com");
		}
	}

	String getUrl() {
		return parameters.getArgs()[0];
	}

	String getOutputDir() {
		return parameters.getArgs()[1];
	}

	private void checkOutDir(String outputDir) {
		File out = new File(outputDir);
		if (out.exists() && out.list().length > 0) {
			if (parameters.hasOption(OVERRIDE)) {
				System.out.println("Overriding output directory...");
				try {
					FileUtils.deleteDirectory(out);
				} catch (IOException e) {
					throw new CrawljaxException(e.getMessage(), e);
				}
			} else {
				throw new IllegalStateException(
				        "Output directory is not empty. If you want to override, use the -override option");
			}
		}
	}

	boolean requestsHelp() {
		return parameters.hasOption(HELP);
	}

	boolean requestsVerbosity() {
		return parameters.hasOption(VERBOSE);
	}

	boolean specifiesLogFile() {
		return parameters.hasOption(LOG_FILE);
	}

	String getSpecifiedLogFile() {
		return parameters.getOptionValue(LOG_FILE);
	}

	boolean specifiesBrowser() {
		return parameters.hasOption(BROWSER);
	}

	BrowserType getSpecifiedBrowser() {
		String browser = parameters.getOptionValue(BROWSER);
		for (BrowserType b : BrowserType.values()) {
			if (b.name().equalsIgnoreCase(browser)) {
				return b;
			}
		}
		throw new IllegalArgumentException("Unrecognized browser: '" + browser
		        + "'. Available browsers are: " + availableBrowsers());
	}

	boolean specifiesParallelBrowsers() {
		return parameters.hasOption(PARALLEL);
	}

	int getSpecifiedNumberOfBrowsers() {
		return Integer.parseInt(parameters.getOptionValue(PARALLEL));
	}

	boolean specifiesDepth() {
		return parameters.hasOption(DEPTH);
	}

	int getSpecifiedDepth() {
		return Integer.parseInt(parameters.getOptionValue(DEPTH));
	}

	boolean specifiesMaxStates() {
		return parameters.hasOption(MAXSTATES);
	}

	int getMaxStates() {
		return Integer.parseInt(parameters.getOptionValue(MAXSTATES));
	}

	boolean requestsCrawlHiddenAnchors() {
		return parameters.hasOption(CRAWL_HIDDEN_ANCHORS);
	}

	boolean specifiesClickElements() {
		return parameters.hasOption(CLICK);
	}

	String[] getSpecifiedClickElements() {
		return parameters.getOptionValue(CLICK).split(",");
	}

	void printHelp() {
		String cmlSyntax = HELP_MESSAGE;
		final PrintWriter writer = new PrintWriter(System.out);
		final HelpFormatter helpFormatter = new HelpFormatter();
		helpFormatter.printHelp(writer, ROW_WIDTH, cmlSyntax, "", options, SPACES_AFTER_OPTION,
		        SPACES_BEFORE_OPTION, "");
		writer.flush();
	}

	boolean specifiesTimeOut() {
		return parameters.hasOption(TIME_OUT);
	}

	long getSpecifiedTimeOut() {
		return Long.parseLong(parameters.getOptionValue(TIME_OUT));
	}

	boolean specifiesWaitAfterEvent() {
		return parameters.hasOption(WAIT_AFTER_EVENT);
	}

	long getSpecifiedWaitAfterEvent() {
		return Long.parseLong(parameters.getOptionValue(WAIT_AFTER_EVENT));
	}

	boolean specifiesWaitAfterReload() {
		return parameters.hasOption(WAIT_AFTER_RELOAD);
	}

	long getSpecifiedWaitAfterReload() {
		return Long.parseLong(parameters.getOptionValue(WAIT_AFTER_RELOAD));
	}

	String getSpecifiedRemoteBrowser() {
		return parameters.getOptionValue(BROWSER_REMOTE_URL);
	}

	// Duplicate Detection arguments
	boolean specifiesUseBroder() {
		return parameters.hasOption(BRODER_DUPLICATE_DETECTION);
	}

	boolean specifiesUseCrawlhash() {
		return parameters.hasOption(CRAWLHASH_DUPLICATE_DETECTION);
	}

	double getSpecifiedThreshold() {
		return Double.parseDouble(parameters.getOptionValue(THRESHOLD));
	}

	int getSpecifiedFeatureSize() {
		return Integer.parseInt(parameters.getOptionValue(FEATURE_SIZE));
	}

	ShingleType getSpecifiedFeatureType() {
		String featureType = parameters.getOptionValue(FEATURE_TYPE);
		for (ShingleType type : ShingleType.values()) {
			String[] args = featureType.split(":");
			if (type.name().equalsIgnoreCase(args[0])) {
				return type;
			}
		}
		throw new IllegalArgumentException("Unrecognized FeatureType: '" + featureType
		        + "'. Available FeatureTypes are: " + availableFeatureTypes());
	}

	boolean specifiesThreshold() {
		return parameters.hasOption(THRESHOLD);
	}

	boolean specifiesFeatureSize() {
		return parameters.hasOption(FEATURE_SIZE);
	}

	boolean specifiesFeatureType() {
		return parameters.hasOption(FEATURE_TYPE);
	}

	String getSpecifiedFeatureRegEx() {
		String featureType = parameters.getOptionValue(FEATURE_TYPE);
		if (getSpecifiedFeatureType() == ShingleType.REGEX) {
			String[] parts = featureType.split(":");
			if (parts.length != 2)
				throw new IllegalArgumentException(
				        "REGEX-type was provided without a valid regular expression. Syntax: regex:<REGEX-HERE> (without <>)");
			return parts[1];
		}
		throw new IllegalArgumentException("Unrecognized Regular Expression: '" + featureType);
	}

	private String availableFeatureTypes() {
		return Joiner.on(", ").join(ShingleType.values());
	}
}
