package com.crawljax.test;

import java.net.URL;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.junit.rules.ExternalResource;

import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.google.common.base.Preconditions;

public class RunWithWebServer extends ExternalResource {

	private final Resource resource;

	private int port;
	private URL demoSite;
	private Server server;
	private boolean started;

	/**
	 * @param classPathResource
	 *            The name of the resource. This resource must be on the test or regular classpath.
	 */
	public RunWithWebServer(String classPathResource) {
		resource = Resource.newClassPathResource(classPathResource);
	}

	@Override
	public void before() throws Exception {
		server = new Server(0);
		ResourceHandler handler = new ResourceHandler();
		handler.setBaseResource(resource);
		server.setHandler(handler);
		server.start();
		this.port = ((ServerConnector) server.getConnectors()[0]).getLocalPort();
		this.demoSite = new URL("http", "localhost", port, "/");
		this.started = true;
	}

	@Override
	public void after() {
		try {
			if (server != null) {
				server.stop();
			}
		} catch (Exception e) {
			throw new RuntimeException("Could not stop the server", e);
		}
	}

	public URL getSiteUrl() {
		checkServerStarted();
		return demoSite;
	}

	public int getPort() {
		checkServerStarted();
		return port;
	}

	public CrawljaxConfigurationBuilder newConfigBuilder() {
		return CrawljaxConfiguration.builderFor(getSiteUrl());
	}

	public CrawljaxConfigurationBuilder newConfigBuilder(String context) {
		return CrawljaxConfiguration.builderFor(getSiteUrl() + context);
	}

	public void stop() throws Exception {
		checkServerStarted();
		server.stop();
	}

	private void checkServerStarted() {
		Preconditions.checkState(started, "Server not started");
	}
}
