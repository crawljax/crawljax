package com.crawljax.web;

import java.io.File;
import java.net.URL;
import java.security.ProtectionDomain;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * Starts the Crawljax server at port 8080.
 */
public class CrawljaxServer {
	private static final Boolean EXECUTE_WAR = true;
	private static String outFolder;

	public static void main(String[] args) throws Exception {
		if (args.length == 0)
			CrawljaxServer.outFolder =
			        System.getProperty("user.home") + File.separatorChar + "crawljax";
		else
			CrawljaxServer.outFolder = args[0];
		System.setProperty("outputFolder", CrawljaxServer.outFolder);

		Server server = new Server(8080);
		HandlerList list = new HandlerList();
		list.addHandler(buildOutputContext(CrawljaxServer.outFolder));
		list.addHandler(buildWebAppContext());
		server.setHandler(list);
		server.start();

		server.join();
	}

	public static WebAppContext buildWebAppContext() throws Exception {
		WebAppContext webAppContext = new WebAppContext();
		webAppContext.setContextPath("/");
		if (EXECUTE_WAR) {
			ProtectionDomain domain = CrawljaxServer.class.getProtectionDomain();
			URL location = domain.getCodeSource().getLocation();
			webAppContext.setWar(location.toExternalForm());
		} else
			webAppContext.setWar(new File("src/main/webapp/").getAbsolutePath());

		return webAppContext;
	}

	public static WebAppContext buildOutputContext(String outputFolder) throws Exception {
		WebAppContext webAppContext = new WebAppContext();
		webAppContext.setContextPath("/output");
		webAppContext.setWar(new File(outputFolder).getAbsolutePath());
		return webAppContext;
	}
}
