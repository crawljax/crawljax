package com.crawljax.web;

import java.io.File;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * Starts the Crawljax server at port 8080.
 */
public class CrawljaxServer {

	public static void main(String[] args) throws Exception {
		Server server = new Server(8080);
		server.setHandler(buildWebAppContext());
		server.start();

		server.join();
	}

	public static WebAppContext buildWebAppContext() throws Exception {
		WebAppContext webAppContext = new WebAppContext();
		webAppContext.setWar(new File("src/main/webapp/").getAbsolutePath());
		return webAppContext;
	}
}
