package com.crawljax.cli;

import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.validator.routines.UrlValidator;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.core.CrawljaxController;
import com.crawljax.core.CrawljaxException;
import com.crawljax.core.configuration.CrawlSpecification;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;

public class JarRunner {

	public static final String VERSION = "version";
	public static final String HELP = "help";
	public static final String MAXSTATES = "maxstates";
	public static final String DEPTH = "depth";
	public static final String BROWSER = "browser";
	public static final String URL = "url";

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
			if (args.length < 1) {
				printHelp(getOptions());
				System.exit(1);
			}
			checkArgumentsAndRun(args);
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

		options.addOption(new Option(HELP, "print this message"));
		options.addOption(new Option(VERSION, "print the version information and exit"));

		options.addOption(OptionBuilder.withArgName("URL").hasArg()
		        .withDescription("url to crawl").create(URL));

		options.addOption(OptionBuilder.withLongOpt(BROWSER)
		        .withDescription("browser type: firefox, chrome, ie, htmlunit").hasArg()
		        .withArgName("TYPE").create());

		options.addOption(OptionBuilder.withLongOpt(DEPTH)
		        .withDescription("crawl depth level").hasArg().withArgName("LEVEL").create());

		options.addOption(OptionBuilder.withLongOpt(MAXSTATES)
		        .withDescription("max number of states to crawl").hasArg()
		        .withArgName("STATES").create());

		return options;
	}

	/**
	 * Write "help" to the provided OutputStream.
	 * 
	 * @param options
	 * @throws IOException
	 */
	public static void printHelp(Options options) throws IOException {
		String cmlSyntax = "java -jar crawljax-cli-" + getCrawljaxVersion() + ".jar";
		final PrintWriter writer = new PrintWriter(System.out);
		final HelpFormatter helpFormatter = new HelpFormatter();
		helpFormatter.printHelp(writer, ROW_WIDTH, cmlSyntax, "", options,
		        SPACES_AFTER_OPTION, SPACES_BEFORE_OPTION, "");
		writer.flush();
	}

	/**
	 * CLI GnuParser to parse command-line arguments.
	 * 
	 * @param args
	 *            Command-line arguments to be processed with Gnu-style parser.
	 * @throws IOException
	 * @throws ParseException
	 * @throws ConfigurationException
	 * @throws CrawljaxException
	 */
	public static void checkArgumentsAndRun(String[] args) throws IOException, ParseException,
	        ConfigurationException, CrawljaxException {

		Options options = getOptions();
		final CommandLine commandLine = new GnuParser().parse(options, args);

		String urlValue = commandLine.getOptionValue(URL);

		if (commandLine.hasOption(HELP)) {
			printHelp(options);
		} else if (commandLine.hasOption(VERSION)) {
			System.out.println("crawljax version \"" + getCrawljaxVersion() + "\"");
		} else if (urlIsInvalid(urlValue)) {
			System.err.println("provide a valid URL. -url=http://example.com");
			printHelp(options);
			System.exit(1);
		} else {
			readConfigAndRun(commandLine, urlValue);
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

	private static void readConfigAndRun(final CommandLine commandLine, String urlValue) {
		CrawlSpecification crawlSpec = new CrawlSpecification(urlValue);
		CrawljaxConfiguration config = new CrawljaxConfiguration();
		config.setCrawlSpecification(crawlSpec);

		if (commandLine.hasOption(BROWSER)) {
			String browser = commandLine.getOptionValue(BROWSER);
			config.setBrowser(getBrowserTypeFromStr(browser));
		}

		if (commandLine.hasOption(DEPTH)) {
			String depth = commandLine.getOptionValue(DEPTH);
			crawlSpec.setDepth(Integer.parseInt(depth));
		}

		if (commandLine.hasOption(MAXSTATES)) {
			String maxstates = commandLine.getOptionValue(MAXSTATES);
			crawlSpec.setMaximumStates(Integer.parseInt(maxstates));
		}

		// run Crawljax
		crawlSpec.clickDefaultElements();
		CrawljaxController crawljax = new CrawljaxController(config);
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
