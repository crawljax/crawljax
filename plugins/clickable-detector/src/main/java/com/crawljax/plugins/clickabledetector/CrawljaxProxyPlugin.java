package com.crawljax.plugins.clickabledetector;

import java.util.List;

import org.littleshoot.proxy.HttpFiltersSource;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.HttpProxyServerBootstrap;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.core.CrawlSession;
import com.crawljax.core.ExitNotifier;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.ProxyConfiguration;
import com.crawljax.core.plugin.PostCrawlingPlugin;
import com.crawljax.core.plugin.PreCrawlingPlugin;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * Starts a proxy server along with Crawljax. The proxy server can be used to monitor traffic and to
 * modify incoming resources.
 */
public class CrawljaxProxyPlugin implements PreCrawlingPlugin, PostCrawlingPlugin {

	private static final Logger LOG = LoggerFactory.getLogger(CrawljaxProxyPlugin.class);
	private final int port;
	private List<HttpFiltersSource> filters = Lists.newArrayList();
	private HttpProxyServer server;

	/**
	 * @param port
	 *            The port you want the proxy to run at.
	 */
	public CrawljaxProxyPlugin(int port) {
		this.port = port;
	}

	/**
	 * Add a filter to the proxy.
	 *
	 * @param filter
	 *            The filter you want to add.
	 * @return The {@link CrawljaxProxyPlugin} for method chaining.
	 */
	public CrawljaxProxyPlugin addFilter(HttpFiltersSource filter) {
		Preconditions.checkState(server == null,
		        "You cannot modify the list once the server has started");
		filters.add(filter);
		return this;
	}

	/**
	 * @return The proxy configuration that should be set in
	 *         {@link com.crawljax.core.configuration .CrawljaxConfiguration.CrawljaxConfigurationBuilder#setProxyConfig(com.crawljax.core.configuration .ProxyConfiguration)}
	 */
	public ProxyConfiguration getConfiguration() {
		return ProxyConfiguration.manualProxyOn("127.0.0.1", port);
	}

	@Override
	public void preCrawling(CrawljaxConfiguration config) throws RuntimeException {
		LOG.info("Starting proxy server with {} filters", filters.size());
		Preconditions.checkState(server == null, "Already started");
		HttpProxyServerBootstrap bootstrap = DefaultHttpProxyServer.bootstrap().withPort(port);

		for (HttpFiltersSource filter : filters) {
			bootstrap.withFiltersSource(filter);
		}

		server = bootstrap.start();
		LOG.debug("Server started at port {}", port);
	}

	@Override
	public void postCrawling(CrawlSession session, ExitNotifier.ExitStatus exitReason) {
		LOG.debug("Proxy server shut-down");
		Preconditions.checkState(server != null, "Not started");
		server.stop();
		LOG.info("Proxy server shut-down complete");
	}

}
