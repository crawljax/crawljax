package com.crawljax.web;

import java.io.File;
import java.net.URL;
import java.security.ProtectionDomain;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * Starts the Crawljax server at port 8080.
 */
public class CrawljaxServer implements Runnable {

	private final boolean EXECUTE_WAR = false;

	private String outputDir;
	private int port;

	private Server server;
	private boolean isRunning;
	private String url = null;

	public CrawljaxServer(String outputDir, int port) {
		this.outputDir = outputDir;
		this.port = port;
		isRunning = false;
	}

	public CrawljaxServer(int port) {
		this.outputDir = System.getProperty("user.home") + File.separatorChar + "crawljax";
		this.port = port;
		isRunning = false;
	}

	@Override
	public void run() {

		System.setProperty("outputFolder", outputDir);

		server = new Server(port);

		HandlerList list = new HandlerList();
		list.addHandler(buildOutputContext());
		list.addHandler(buildWebAppContext());
		server.setHandler(list);

		try {
			server.start();
		} catch (Exception e) {
			throw new RuntimeException("Could not start Crawljax web server", e);
		}

		port = ((ServerConnector) server.getConnectors()[0]).getLocalPort(); //Value will change if originally set to 0
		url = "http://localhost:" + port;

		isRunning = true;
		try {
			server.join();
		} catch (InterruptedException e) {
			throw new RuntimeException("Crawljax server interrupted", e);
		}
		isRunning = false;
	}

	public void stop() {
		try {
			server.stop();
		} catch (Exception e) {
			throw new RuntimeException("Could not stop Crawljax web server", e);
		}
	}

	public boolean isRunning() {
		return isRunning;
	}

	public String getUrl() {
		return url;
	}

	private WebAppContext buildWebAppContext() {
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

	private WebAppContext buildOutputContext() {
		WebAppContext webAppContext = new WebAppContext();
		webAppContext.setContextPath("/output");
		webAppContext.setWar(new File(outputDir).getAbsolutePath());
		return webAppContext;
	}
}
