package com.crawljax.demo;

import java.net.URL;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.junit.rules.ExternalResource;
import com.google.common.base.Preconditions;

public class RunWithDemoServer extends ExternalResource {

	private int port;
	private URL demoSite;
	private Server server;
	private boolean started;

	@Override
	protected void before() throws Throwable {
		server = new Server(0);
		ResourceHandler handler = new ResourceHandler();
		handler.setBaseResource(Resource.newClassPathResource("/demo-site"));
		server.setHandler(handler);
		server.start();
		this.port = server.getConnectors()[0].getLocalPort();
		this.demoSite = new URL("http", "localhost", port, "/");
		this.started = true;
	}

	@Override
	protected void after() {
		try {
			if (server != null) {
				server.stop();
			}
		} catch (Exception e) {
			throw new RuntimeException("Could not stop the server", e);
		}
	}

	public URL getDemoSite() {
		checkServerStarted();
		return demoSite;
	}

	public int getPort() {
		checkServerStarted();
		return port;
	}

	public void stop() throws Exception {
		checkServerStarted();
		server.stop();
	}

	private void checkServerStarted() {
		Preconditions.checkState(started, "Server not started");
	}
}
