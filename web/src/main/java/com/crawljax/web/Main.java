package com.crawljax.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class Main {

	private static final Logger LOG = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) throws Exception {

		final ParameterInterpeter options = new ParameterInterpeter(args);

		String outFolder = options.specifiesOutputDir() ? options.getSpecifiedOutputDir() : "out";

		int port = options.specifiesPort() ? options.getSpecifiedPort() : 8080;

		final CrawljaxServer server = new CrawljaxServer(port, new File(outFolder));

		Runtime.getRuntime().addShutdownHook(new Thread() {

			@Override
			public void run() {
				LOG.info("Shutdown hook initiated");
				try {
					server.stop();
				} catch (Exception e) {
					LOG.warn("Could not stop the server in properly {}", e.getMessage());
					LOG.debug("Stop error was ", e);
				}
			}
		});

		server.start(true);
	}
}
