package com.crawljax.web;

import java.io.File;

public class Main {

	public static void main(String[] args) throws Exception {

		final ParameterInterpeter options = new ParameterInterpeter(args);

		String outFolder = options.specifiesOutputDir() ? options.getSpecifiedOutputDir() :
				System.getProperty("user.home") + File.separatorChar + "crawljax";

		int port = options.specifiesPort() ? options.getSpecifiedPort() : 8080;

		CrawljaxServer server = new CrawljaxServer(outFolder, port);
		server.run();
	}
}
