package com.crawljax.cli;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.validator.routines.UrlValidator;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.core.CrawljaxController;
import com.crawljax.core.CrawljaxException;
import com.crawljax.core.configuration.CrawlSpecification;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.google.common.base.Strings;

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
	private static String CRAWLJAX_VERSION;

	private final static CrawljaxConfiguration config = new CrawljaxConfiguration();
	private static CrawlSpecification crawlSpec;
	private static Options options;

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
	public static void checkArguments(String[] args) throws IOException, ParseException,
	        ConfigurationException, CrawljaxException {

		final CommandLine commandLine = new GnuParser().parse(getOptions(), args);

		if (commandLine.hasOption(HELP)) {
			printHelp();
			System.exit(0);
		}

		if (commandLine.hasOption(VERSION)) {
			System.out.println("crawljax version \"" + getCrawljaxVersion() + "\"");
			System.exit(0);
		}

		// URL is required
		if (commandLine.hasOption(URL)) {

			String urlValue = commandLine.getOptionValue(URL);

			final String[] schemes = { "http", "https" };

			if (new UrlValidator(schemes).isValid(urlValue)) {

				crawlSpec = new CrawlSpecification(urlValue);
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

			} else {
				System.err.println("url is invalid: " + urlValue);
			}

		} else {
			System.err.println("provide a valid URL. -url=http://example.com");
			printHelp();
			System.exit(1);
		}
	}

	private static BrowserType getBrowserTypeFromStr(String browser) {
		if (browser != null) {
			for (BrowserType b : BrowserType.values()) {
				if (browser.equalsIgnoreCase(b.toString())) {
					return b;
				}
			}
		}

		return BrowserType.firefox;
	}

	/**
	 * Create the CML Options.
	 * 
	 * @return Options expected from command-line.
	 */
	private static Options getOptions() {

		if (options == null) {
			options = new Options();

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
		}

		return options;
	}

	/**
	 * Write "help" to the provided OutputStream.
	 * 
	 * @throws IOException
	 */
	public static void printHelp() throws IOException {
		String cmlSyntax = "java -jar crawljax-cli-" + getCrawljaxVersion() + ".jar";
		final PrintWriter writer = new PrintWriter(System.out);
		final HelpFormatter helpFormatter = new HelpFormatter();
		helpFormatter.printHelp(writer, ROW_WIDTH, cmlSyntax, "", getOptions(),
		        SPACES_AFTER_OPTION, SPACES_BEFORE_OPTION, "");
		writer.flush();
	}

	private static String getCrawljaxVersion() throws IOException {
		if (Strings.isNullOrEmpty(CRAWLJAX_VERSION)) {
			StringWriter writer = new StringWriter();
			IOUtils.copy(JarRunner.class.getResourceAsStream("/project.version"), writer);
			CRAWLJAX_VERSION = writer.toString();
		}

		return CRAWLJAX_VERSION;
	}

	/**
	 * Main executable method of Crawljax CLI.
	 * 
	 * @param args
	 *            the arguments.
	 */
	public static void main(String[] args) {

		try {
			if (args.length < 1) {
				printHelp();
				System.exit(1);
			}

			checkArguments(args);

		} catch (Exception e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
	}
}
