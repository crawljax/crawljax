package com.crawljax.cli;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.io.FileUtils;
import org.apache.commons.validator.routines.UrlValidator;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.core.CrawljaxController;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.plugins.crawloverview.CrawlOverview;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;

public class JarRunner {

	public static final String VERSION = "version";
	public static final String HELP = "help";
	public static final String MAXSTATES = "maxstates";
	public static final String DEPTH = "depth";
	public static final String BROWSER = "browser";
	public static final String PARALLEL = "parallel";
	public static final String OVERRIDE = "override";

	private static final int SPACES_AFTER_OPTION = 3;
	private static final int SPACES_BEFORE_OPTION = 5;
	private static final int ROW_WIDTH = 80;

	/**
	 * Main executable method of Crawljax CLI.
	 * 
	 * @param args
	 *            the arguments.
	 */
	public static void main(String[] args) {
		try {
			Options options = getOptions();
			final CommandLine commandLine = new GnuParser().parse(options, args);
			if (commandLine.hasOption(HELP)) {
				printHelp(options);
			} else if (commandLine.hasOption(VERSION)) {
				System.out.println(getCrawljaxVersion());
			} else if (args.length >= 2) {
				String url = commandLine.getArgs()[0];
				String outputDir = commandLine.getArgs()[1];
				if (urlIsInvalid(url)) {
					System.err.println("provide a valid URL like http://example.com");
					System.exit(1);
				} else {
					checkOutDir(commandLine, outputDir);
					readConfigAndRun(commandLine, url, outputDir);
				}
			} else {
				printHelp(options);
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
	}

	/**
	 * Create the CML Options.
	 * 
	 * @return Options expected from command-line.
	 */
	private static Options getOptions() {
		Options options = new Options();
		options.addOption("h", HELP, false, "print this message");
		options.addOption("v", VERSION, false, "print the version information and exit");

		options.addOption("b", "browser", true,
		        "browser type: firefox, chrome, ie, htmlunit. Default is Firefox");

		options.addOption("d", DEPTH, true, "crawl depth level. Default is 2");

		options.addOption("s", MAXSTATES, true,
		        "max number of states to crawl. Default is 0 (unlimited)");

		options.addOption("p", PARALLEL, true,
		        "Number of browsers to use for crawling. Default is 1");
		options.addOption("o", OVERRIDE, false, "Override the output directory if non-empty");
		return options;
	}

	/**
	 * Write "help" to the provided OutputStream.
	 * 
	 * @param options
	 * @throws IOException
	 */
	public static void printHelp(Options options) throws IOException {
		String cmlSyntax = "java -jar crawljax-cli-version.jar theUrl theOutputDir";
		final PrintWriter writer = new PrintWriter(System.out);
		final HelpFormatter helpFormatter = new HelpFormatter();
		helpFormatter.printHelp(writer, ROW_WIDTH, cmlSyntax, "", options, SPACES_AFTER_OPTION,
		        SPACES_BEFORE_OPTION, "");
		writer.flush();
	}

	private static void checkOutDir(final CommandLine commandLine, String outputDir)
	        throws IOException {
		File out = new File(outputDir);
		if (out.exists() && out.list().length > 0) {
			if (commandLine.hasOption(OVERRIDE)) {
				System.out.println("Overriding output directory...");
				FileUtils.deleteDirectory(out);
			} else {
				System.out
				        .println("Output directory is not empty. If you want to override, use the -override option");
				System.exit(1);
			}
		}
	}

	private static String getCrawljaxVersion() throws IOException {
		return Resources
		        .toString(JarRunner.class.getResource("/project.version"), Charsets.UTF_8);
	}

	private static boolean urlIsInvalid(String urlValue) {
		final String[] schemes = { "http", "https" };
		return urlValue == null || !new UrlValidator(schemes).isValid(urlValue);
	}

	private static void readConfigAndRun(final CommandLine commandLine, String urlValue,
	        String outputDir) {
		CrawljaxConfigurationBuilder builder = CrawljaxConfiguration.builderFor(urlValue);
		builder.addPlugin(new CrawlOverview(new File(outputDir)));

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

		// run Crawljax
		builder.crawlRules().clickDefaultElements();
		CrawljaxController crawljax = new CrawljaxController(builder.build());
		crawljax.run();
	}

	private static BrowserType getBrowserTypeFromStr(String browser) {
		if (browser != null) {
			for (BrowserType b : BrowserType.values()) {
				if (browser.equalsIgnoreCase(b.toString())) {
					return b;
				}
			}
		}
		System.out.println("Unrecognized browser " + browser + ". Using firefox instead");
		return BrowserType.firefox;
	}
}
