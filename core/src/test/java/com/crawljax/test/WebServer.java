package com.crawljax.test;

import java.net.URI;

import com.google.common.base.Preconditions;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.Resource;

public class WebServer {
	private final Resource resource;

	private int port;
	private URI demoSite;
	private Server server;
	private boolean started;

	/**
	 * @param classPathResource
	 *            The name of the resource. This resource must be on the test or regular classpath.
	 */
	public WebServer(Resource classPathResource) {
		resource = classPathResource;
	}

	public void start() throws Exception {
		server = new Server(0);
		ResourceHandler handler = new ResourceHandler();
		handler.setBaseResource(resource);
		server.setHandler(handler);
		server.start();
		this.port = ((ServerConnector) server.getConnectors()[0]).getLocalPort();
		this.demoSite = URI.create("http://localhost:" + port + "/");
		this.started = true;
	}

	public URI getSiteUrl() {
		checkServerStarted();
		return demoSite;
	}

	public int getPort() {
		checkServerStarted();
		return port;
	}

	public void stop() {
		checkServerStarted();
		try {
			server.stop();
		} catch (Exception e) {
			throw new RuntimeException("Could not stop the server", e);
		}
	}

	private void checkServerStarted() {
		Preconditions.checkState(started, "Server not started");
	}

	public void join() throws InterruptedException {
		server.join();
	}
}
