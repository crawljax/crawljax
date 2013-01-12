package com.crawljax.cli;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.validator.routines.UrlValidator;

import com.crawljax.core.CrawljaxController;
import com.crawljax.core.CrawljaxException;
import com.crawljax.core.configuration.CrawlSpecification;
import com.crawljax.core.configuration.CrawljaxConfiguration;

public class JarRunner {

	private static String CRAWLJAX_VERSION;
	private final static CrawljaxConfiguration config = new CrawljaxConfiguration();
	private static Options options;

	/**
	 * CLI GnuParser to parse command-line arguments.
	 * 
	 * @param commandLineArguments
	 *            Command-line arguments to be processed with Gnu-style parser.
	 * @throws IOException
	 * @throws ParseException
	 * @throws ConfigurationException
	 * @throws CrawljaxException
	 */
	public static void useGnuParser(final String[] commandLineArguments) throws IOException,
	        ParseException, ConfigurationException, CrawljaxException {

		getCrawljaxVersion();

		final CommandLineParser cmdLineGnuParser = new GnuParser();

		final Options gnuOptions = getOptions();

		CommandLine commandLine = cmdLineGnuParser.parse(gnuOptions, commandLineArguments);

		if (commandLine.hasOption("help")) {
			printHelp(getOptions(), 80, "", "", 5, 3, System.out);
			System.exit(0);
		}

		if (commandLine.hasOption("version")) {
			System.out.println("crawljax version \"" + CRAWLJAX_VERSION + "\"");
			System.exit(0);

		}

		if (commandLine.hasOption("url")) {

			String url = commandLine.getOptionValue("url");

			final String[] schemes = { "http", "https" };

			if (new UrlValidator(schemes).isValid(url)) {
				final CrawlSpecification crawlSpec = new CrawlSpecification(url);
				config.setCrawlSpecification(crawlSpec);

				if (commandLine.hasOption("depth")) {
					String depth = commandLine.getOptionValue("depth");
					crawlSpec.setDepth(Integer.parseInt(depth));
				}

				if (commandLine.hasOption("maxstates")) {
					String maxstates = commandLine.getOptionValue("maxstates");
					crawlSpec.setMaximumStates(Integer.parseInt(maxstates));
				}

				crawlSpec.clickDefaultElements();

				// run Crawljax
				CrawljaxController crawljax = new CrawljaxController(config);
				crawljax.run();

			} else {
				System.err.println("url is invalid: " + url);
			}

		} else {
			System.err.println("provide a valid URL. -url=http://example.com");
			printHelp(getOptions(), 80, "", "", 5, 3, System.out);
			System.exit(1);
		}

	}

	/**
	 * Create the CML Options.
	 * 
	 * @return Options expected from command-line.
	 */
	private static Options getOptions() {

		if (options == null) {
			options = new Options();

			options.addOption(new Option("help", "print this message"));
			options.addOption(new Option("version", "print the version information and exit"));

			options.addOption(OptionBuilder.withArgName("URL").hasArg()
			        .withDescription("url to crawl").create("url"));

			options.addOption(OptionBuilder.withLongOpt("depth")
			        .withDescription("crawl depth level").hasArg().withArgName("LEVEL").create());

			options.addOption(OptionBuilder.withLongOpt("maxstates")
			        .withDescription("max number of states to crawl").hasArg()
			        .withArgName("STATES").create());
		}

		return options;
	}

	/**
	 * Write "help" to the provided OutputStream.
	 */
	public static void printHelp(final Options options, final int printedRowWidth,
	        final String header, final String footer, final int spacesBeforeOption,
	        final int spacesBeforeOptionDescription, final OutputStream out) {

		final String commandLineSyntax = "java -jar crawljax-cli-" + CRAWLJAX_VERSION + ".jar";

		final PrintWriter writer = new PrintWriter(out);
		final HelpFormatter helpFormatter = new HelpFormatter();
		helpFormatter.printHelp(writer, printedRowWidth, commandLineSyntax, header, options,
		        spacesBeforeOption, spacesBeforeOptionDescription, footer);
		writer.flush();
	}

	private static void getCrawljaxVersion() throws IOException {
		if (CRAWLJAX_VERSION == null || "".equals(CRAWLJAX_VERSION)) {
			StringWriter writer = new StringWriter();
			IOUtils.copy(JarRunner.class.getResourceAsStream("/project.version"), writer);
			CRAWLJAX_VERSION = writer.toString();
		}
	}

	/**
	 * Main executable method of Crawljax CLI.
	 * 
	 * @param args
	 *            the arguments.
	 */
	public static void main(String[] args) {

		if (args.length < 1) {
			printHelp(getOptions(), 80, "", "", 5, 3, System.out);
			System.exit(1);
		}

		try {
			useGnuParser(args);
		} catch (IOException e) {
			System.err.println(e.getMessage());
		} catch (ConfigurationException e) {
			System.err.println(e.getMessage());
		} catch (ParseException e) {
			System.err.println(e.getMessage());
		} catch (CrawljaxException e) {
			System.err.println(e.getMessage());
		} finally {
			System.exit(1);
		}
	}
}
