package com.crawljax.crawljax_plugins_plugin;

import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jetty.util.resource.Resource;

import com.crawljax.core.CrawlSession;
import com.crawljax.core.CrawljaxController;
import com.crawljax.core.configuration.CrawlSpecification;
import com.crawljax.core.configuration.CrawljaxConfiguration;

/**
 * Base class for Crawler examples.
 */
public abstract class SampleCrawler {

	private final WebServer webServer;
	private final AtomicBoolean hasSetup = new AtomicBoolean(false);

	private CrawljaxConfiguration config;
	private CrawlSpecification crawlSpec;
	private String siteExtension;

	protected SampleCrawler(String siteExtension) {
		this.siteExtension = siteExtension;
		this.webServer = new WebServer(Resource.newClassPathResource("/sites"));
	}

	public WebServer getWebServer() {
		return webServer;
	}

	/**
	 * Starts the webserver, configures crawljax. Doesn't run the Crawler. For that you need to cal
	 * {@link #crawl()}. After this method completes, you can configure the
	 * {@link CrawlSpecification} and the {@link CrawljaxConfiguration} using {@link #getConfig()}
	 * and {@link #getCrawlSpec()}.
	 * <p>
	 * The {@link CrawlSpecification} is configured with
	 * {@link CrawlSpecification#clickDefaultElements()}.
	 * 
	 * @throws Exception
	 *             When the webserver fails to start.
	 */
	public void setup() throws Exception {
		webServer.start();
		crawlSpec = new CrawlSpecification(getUrl());
		crawlSpec.clickDefaultElements();
		config = new CrawljaxConfiguration();
		config.setCrawlSpecification(crawlSpec);
		hasSetup.set(true);
	}

	private String getUrl() {
		return webServer.getSiteUrl().toExternalForm() + siteExtension;
	}

	/**
	 * @return the {@link CrawljaxConfiguration} or <code>null</code> if {@link #setup()} or
	 *         {@link #crawl()} haven't been called yet.
	 */
	public CrawljaxConfiguration getConfig() {
		return config;
	}

	/**
	 * @return the {@link CrawlSpecification} or <code>null</code> if {@link #setup()} or
	 *         {@link #crawl()} haven't been called yet.
	 */
	public CrawlSpecification getCrawlSpec() {
		return crawlSpec;
	}

	/**
	 * Runs the crawler. If you haven't run {@link #setup()} it will do that before it runs.
	 * 
	 * @return {@link CrawlSession} for post-crawl inspection.
	 * @throws Exception
	 *             When crawljax isn't configured correctly or the webserver fails to stop.
	 */
	public CrawlSession crawl() throws Exception {
		if (!hasSetup.get()) {
			setup();
		}
		CrawljaxController crawljax = new CrawljaxController(config);
		crawljax.run();
		webServer.stop();
		crawljax.getBrowserPool().close();
		return crawljax.getSession();
	}

	/**
	 * This starts the webserver and blocks the thread so you can inspect the site in your browser
	 * manually. The URL to visit is printed to the console.
	 * <p>
	 * This thread will block once started so you have to stop it manually, once you are done.
	 * 
	 * @throws Exception
	 *             in case the webserver can't start
	 */
	public void showWebSite() throws Exception {
		webServer.start();
		System.out.println(getUrl());
		webServer.join();
	}

}
