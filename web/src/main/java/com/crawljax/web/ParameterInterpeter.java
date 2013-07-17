package com.crawljax.web;

import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.GnuParser;

public class ParameterInterpeter {

	static final String OUTPUT_DIR = "outputDir";
	static final String PORT = "port";

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

		options.addOption("o", OUTPUT_DIR, true, "Specify output directory.");
		options.addOption("p", PORT, true, "Specify port. Default is 8080.");

		return options;
	}

	boolean specifiesOutputDir() {
		return parameters.hasOption(OUTPUT_DIR);
	}

	String getSpecifiedOutputDir() {
		return parameters.getOptionValue(OUTPUT_DIR).trim();
	}

	boolean specifiesPort() {
		return parameters.hasOption(PORT);
	}

	int getSpecifiedPort() {
		return Integer.parseInt(parameters.getOptionValue(PORT).trim());
	}
}
