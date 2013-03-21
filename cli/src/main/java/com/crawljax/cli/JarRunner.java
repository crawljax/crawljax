package com.crawljax.cli;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.validator.routines.UrlValidator;

import ch.qos.logback.classic.Level;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.core.CrawljaxController;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.core.configuration.CrawlRules;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.plugins.crawloverview.CrawlOverview;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.io.Files;
import com.google.common.io.Resources;

public class JarRunner {

	static final String MISSING_ARGUMENT_MESSAGE =
	        "Missing required argument URL and/or output folder.";

	static final String HELP_MESSAGE =
	        "java -jar crawljax-cli-version.jar theUrl theOutputDir";

	static final String VERSION = "version";
	static final String VERBOSE = "verbose";
	static final String HELP = "help";
	static final String MAXSTATES = "maxstates";
	static final String DEPTH = "depth";
	static final String BROWSER = "browser";
	static final String PARALLEL = "parallel";
	static final String OVERRIDE = "override";
	static final String CRAWL_HIDDEN_ANCHORS = "crawlHiddenAnchors";
	static final String TIME_OUT = "timeout";
	static final String WAIT_AFTER_RELOAD = "waitAfterReload";
	static final String WAIT_AFTER_EVENT = "waitAfterEvent";
	static final String LOG_FILE = "log";

	static final String CLICK = "click";

	private static final int SPACES_AFTER_OPTION = 3;
	private static final int SPACES_BEFORE_OPTION = 5;
	private static final int ROW_WIDTH = 80;

	private final CommandLine commandLine;

	private Options options;

	private CrawljaxConfiguration config;

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
		options = getOptions();
		try {
			commandLine = new GnuParser().parse(options, args);
		} catch (ParseException e) {
			throw new IllegalArgumentException(e.getMessage(), e);
		}
		if (commandLine.hasOption(VERSION)) {
			System.out.println(getCrawljaxVersion());
		} else if (commandLine.getArgs().length == 2) {
			String url = commandLine.getArgs()[0];
			String outputDir = commandLine.getArgs()[1];
			checkUrlValidity(url);
			checkOutDir(outputDir);
			configureLogging();
			this.config = readConfig(url, outputDir);
		} else {
			if (!commandLine.hasOption(HELP)) {
				System.out.println(MISSING_ARGUMENT_MESSAGE);
			}
			printHelp();
		}
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

		options.addOption("b", "browser", true,
		        "browser type: " + availableBrowsers() + ". Default is Firefox");

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

		return options;
	}

	private String availableBrowsers() {
		return Joiner.on(", ").join(BrowserType.values());
	}

	private String getCrawljaxVersion() {
		try {
			return Resources
			        .toString(JarRunner.class.getResource("/project.version"), Charsets.UTF_8);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	private void printHelp() {
		String cmlSyntax = HELP_MESSAGE;
		final PrintWriter writer = new PrintWriter(System.out);
		final HelpFormatter helpFormatter = new HelpFormatter();
		helpFormatter.printHelp(writer, ROW_WIDTH, cmlSyntax, "", options, SPACES_AFTER_OPTION,
		        SPACES_BEFORE_OPTION, "");
		writer.flush();
	}

	private void checkUrlValidity(String urlValue) {
		String[] schemes = { "http", "https" };
		if (urlValue == null || !new UrlValidator(schemes).isValid(urlValue)) {
			throw new IllegalArgumentException("provide a valid URL like http://example.com");
		}
	}

	private void checkOutDir(String outputDir) {
		File out = new File(outputDir);
		if (out.exists() && out.list().length > 0) {
			if (commandLine.hasOption(OVERRIDE)) {
				System.out.println("Overriding output directory...");
				try {
					FileUtils.deleteDirectory(out);
				} catch (IOException e) {
					throw new RuntimeException(e.getMessage(), e);
				}
			} else {
				throw new IllegalStateException(
				        "Output directory is not empty. If you want to override, use the -override option");
			}
		}
	}

	private void configureLogging() {
		if (commandLine.hasOption(VERBOSE)) {
			LogUtil.setCrawljaxLogLevel(Level.INFO);
		}
		if (commandLine.hasOption(LOG_FILE)) {
			File f = new File(commandLine.getOptionValue(LOG_FILE));
			try {
				if (!f.exists()) {
					Files.createParentDirs(f);
					Files.touch(f);
				}
			} catch (IOException e) {
				throw new RuntimeException("Could not create log file: " + e.getMessage(), e);
			}
			Preconditions.checkArgument(f.canWrite());
			LogUtil.logToFile(f.getPath());
		}

	}

	private CrawljaxConfiguration readConfig(String urlValue, String outputDir) {
		CrawljaxConfigurationBuilder builder = CrawljaxConfiguration.builderFor(urlValue);

		BrowserType browser = BrowserType.firefox;
		if (commandLine.hasOption(BROWSER)) {
			String browserString = commandLine.getOptionValue(BROWSER);
			browser = getBrowserTypeFromStr(browserString);
		}

		int browsers = 1;
		if (commandLine.hasOption(PARALLEL)) {
			browsers = Integer.parseInt(commandLine.getOptionValue(PARALLEL));
		}
		builder.setBrowserConfig(new BrowserConfiguration(browser, browsers));

		if (commandLine.hasOption(DEPTH)) {
			String depth = commandLine.getOptionValue(DEPTH);
			builder.setMaximumDepth(Integer.parseInt(depth));
		}

		if (commandLine.hasOption(MAXSTATES)) {
			String maxstates = commandLine.getOptionValue(MAXSTATES);
			builder.setMaximumStates(Integer.parseInt(maxstates));
		}

		if (commandLine.hasOption(CRAWL_HIDDEN_ANCHORS)) {
			builder.crawlRules().crawlHiddenAnchors(true);
		}

		configureTimers(builder);

		builder.addPlugin(new CrawlOverview(new File(outputDir)));

		if (commandLine.hasOption(CLICK)) {
			builder.crawlRules().click(commandLine.getOptionValue(CLICK).split(","));
		} else {
			builder.crawlRules().clickDefaultElements();
		}

		return builder.build();
	}

	private void configureTimers(CrawljaxConfigurationBuilder builder) {
		if (commandLine.hasOption(TIME_OUT)) {
			long time = Long.parseLong(commandLine.getOptionValue(TIME_OUT));
			builder.setMaximumRunTime(time, TimeUnit.MINUTES);
		}
		if (commandLine.hasOption(WAIT_AFTER_EVENT)) {
			long time = Long.parseLong(commandLine.getOptionValue(WAIT_AFTER_EVENT));
			builder.crawlRules().waitAfterEvent(time, TimeUnit.MILLISECONDS);
		}
		if (commandLine.hasOption(WAIT_AFTER_RELOAD)) {
			long time = Long.parseLong(commandLine.getOptionValue(WAIT_AFTER_RELOAD));
			builder.crawlRules().waitAfterReloadUrl(time, TimeUnit.MILLISECONDS);
		}
	}

	private BrowserType getBrowserTypeFromStr(String browser) {
		if (browser != null) {
			for (BrowserType b : BrowserType.values()) {
				if (browser.equalsIgnoreCase(b.toString())) {
					return b;
				}
			}
		}
		throw new IllegalArgumentException("Unrecognized browser: '" + browser
		        + "'. Available browsers are: " + availableBrowsers());
	}

	private void runIfConfigured() {
		if (config != null) {
			CrawljaxController crawljax = new CrawljaxController(config);
			crawljax.run();
		}
	}

	@VisibleForTesting
	CrawljaxConfiguration getConfig() {
		return config;
	}
}
