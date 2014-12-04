package com.crawljax.test;

import java.net.URI;

import com.crawljax.browser.BrowserProvider;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.google.common.base.Preconditions;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.junit.rules.ExternalResource;

public class RunWithWebServer extends ExternalResource {

	private final Resource resource;

	private int port;
	private URI demoSite;
	private Server server;
	private boolean started;

	/**
	 * @param classPathResource The name of the resource. This resource must be on the test or regular classpath.
	 */
	public RunWithWebServer(String classPathResource) {
		resource = Resource.newClassPathResource(classPathResource);
	}

	@Override
	public void before() throws Exception {
		server = newWebServer();
		server.start();
		this.port = ((ServerConnector) server.getConnectors()[0]).getLocalPort();
		this.demoSite = URI.create("http://localhost:" + port + "/");
		this.started = true;
	}

	/**
	 * Override this method to configure custom server settings.
	 *
	 * @return a {@link Server}.
	 */
	protected Server newWebServer() {
		Server server = new Server(0);
		ResourceHandler handler = new ResourceHandler();
		handler.setBaseResource(resource);
		server.setHandler(handler);
		return server;
	}

	@Override
	public void after() {
		try {
			if (server != null) {
				server.stop();
			}
		}
		catch (Exception e) {
			throw new RuntimeException("Could not stop the server", e);
		}
	}

	public URI getSiteUrl() {
		checkServerStarted();
		return demoSite;
	}

	public int getPort() {
		checkServerStarted();
		return port;
	}

	public CrawljaxConfigurationBuilder newConfigBuilder() {
		return CrawljaxConfiguration.builderFor(getSiteUrl())
		                            .setBrowserConfig(new BrowserConfiguration(BrowserProvider.getBrowserType()));
	}

	public CrawljaxConfigurationBuilder newConfigBuilder(String context) {
		return CrawljaxConfiguration.builderFor(getSiteUrl() + context)
		                            .setBrowserConfig(new BrowserConfiguration(BrowserProvider.getBrowserType()));
	}

	public void stop() throws Exception {
		checkServerStarted();
		server.stop();
	}

	private void checkServerStarted() {
		Preconditions.checkState(started, "Server not started");
	}
}
