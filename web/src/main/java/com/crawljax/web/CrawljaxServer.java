package com.crawljax.web;

import java.io.File;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.web.di.CrawljaxWebModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceFilter;
import com.google.inject.servlet.GuiceServletContextListener;

/**
 * Starts the Crawljax server at port 8080.
 */
public class CrawljaxServer {

	private static final Logger LOG = LoggerFactory.getLogger(CrawljaxServer.class);

	public static void main(String[] args) throws Exception {
		File outputFolder = new File("out");
		CrawljaxServer server = new CrawljaxServer(8080, outputFolder);
		server.start(true);
	}

	private Server server;

	public CrawljaxServer(int port, final File outputFolder) {
		server = new Server(port);

		WebAppContext webAppContext = new WebAppContext();
		webAppContext.setContextPath("/");

		webAppContext.setBaseResource(Resource.newClassPathResource("web"));
		webAppContext.setParentLoaderPriority(true);
		webAppContext.addEventListener(new GuiceServletContextListener() {

			@Override
			protected Injector getInjector() {
				return Guice.createInjector(new CrawljaxWebModule(outputFolder));
			}

		});

		webAppContext.addFilter(GuiceFilter.class, "/*", null);

		server.setHandler(webAppContext);

	}

	/**
	 * @param join
	 *            If you want to merge with the remote thread.
	 * @throws Exception
	 *             When the server can't start.
	 * @see Thread#join()
	 */
	public void start(boolean join) throws Exception {
		LOG.info("Starting the server");
		server.start();
		LOG.info("Server started");
		if (join) {
			LOG.debug("Joining server thread");
			server.join();
		}
	}

	public void stop() throws Exception {
		LOG.info("Stopping the server");
		server.stop();
		LOG.info("Shutdown complete");
	}

	public int getPort() {
		return ((ServerConnector) server.getConnectors()[0]).getLocalPort();
	}
}
