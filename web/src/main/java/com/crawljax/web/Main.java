package com.crawljax.web;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class Main {

	private static final Logger LOG = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) throws Exception {

		final ParameterInterpeter options = new ParameterInterpeter(args);

		String outFolder = options.specifiesOutputDir() ? options.getSpecifiedOutputDir() : "out";

		int port = options.specifiesPort() ? options.getSpecifiedPort() : 8080;

		final CrawljaxServer server = new CrawljaxServer(new CrawljaxServerConfigurationBuilder()
				.setPort(port).setOutputDir(new File(outFolder)));

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

	public static String getCrawljaxVersion() {
		try {
			String[] lines = Resources.toString(Main.class.getResource("/crawljax.version"), Charsets.UTF_8)
					.split(System.getProperty("line.separator"));
			for(String line : lines) {
				String[] keyValue = line.split("=");
				if(keyValue[0].trim().toLowerCase().equals("version")) {
					return keyValue[1].trim();
				}
			}
			return null;
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
}
