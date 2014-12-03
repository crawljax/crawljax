package com.crawljax.web;

import java.io.File;

public class Main {

	public static void main(String[] args) throws Exception {

		String outFolder = "out";

		int port = 8080;

		final CrawljaxServer server = new CrawljaxServer(new CrawljaxServerConfigurationBuilder()
				.setPort(port).setOutputDir(new File(outFolder)));

		Runtime.getRuntime().addShutdownHook(new Thread() {

			@Override
			public void run() {
				try {
					server.stop();
				} catch (Exception e) {
				}
			}
		});

		server.start(true);
	}

	public static String getCrawljaxVersion() {
		try {
			return "3.6-SNAPSHOT";
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
}
