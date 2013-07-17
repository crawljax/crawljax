package com.crawljax.web;

import java.io.File;
import java.net.URL;
import java.security.ProtectionDomain;
import java.util.concurrent.*;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * Starts the Crawljax server at port 8080.
 */
public class CrawljaxServer implements Callable<Void> {

	private final boolean EXECUTE_WAR = false;

	private File outputDir;
	private int port;

	private Server server;
	private final CountDownLatch isRunningLatch = new CountDownLatch(1);
	private String url = null;

	public CrawljaxServer(File outputDir, int port) {
		this.outputDir = outputDir;
		this.port = port;
	}

	public CrawljaxServer(int port) {
		this.outputDir = new File(System.getProperty("user.home") + File.separatorChar + "crawljax");
		this.port = port;
	}

	@Override
	public Void call() {
		run();
		return null;
	}

	public void run() {

		System.setProperty("outputFolder", outputDir.getAbsolutePath());

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

		isRunningLatch.countDown();
		try {
			server.join();
		} catch (InterruptedException e) {
			throw new RuntimeException("Crawljax server interrupted", e);
		}
	}

	public void waitUntilRunning(long timeOut_ms) {
		try {
			isRunningLatch.await(timeOut_ms, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public void stop() {
		try {
			server.stop();
		} catch (Exception e) {
			throw new RuntimeException("Could not stop Crawljax web server", e);
		}
	}

	public String getUrl() {
		return url;
	}

	public File getOutputDir() {
		return outputDir;
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
		webAppContext.setWar(outputDir.getAbsolutePath());
		return webAppContext;
	}
}
