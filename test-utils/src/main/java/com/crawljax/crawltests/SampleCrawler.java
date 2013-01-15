package com.crawljax.crawltests;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jetty.util.resource.Resource;

import com.crawljax.core.CrawlSession;
import com.crawljax.core.CrawljaxController;
import com.crawljax.core.CrawljaxException;
import com.crawljax.core.configuration.CrawlSpecification;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.google.common.base.Strings;

/**
 * Base class for Crawler examples.
 */
public abstract class SampleCrawler {

	private final WebServer webServer;
	private final AtomicBoolean hasSetup = new AtomicBoolean(false);

	private CrawljaxConfiguration config;
	private CrawlSpecification crawlSpec;
	private String siteExtension;

	/**
	 * @param siteExtension
	 *            Assumes the sites are in <code>resources/sites</code>.
	 */
	protected SampleCrawler(String siteExtension) {
		this.siteExtension = siteExtension;
		URL sampleSites = SampleCrawler.class.getResource("/sites");
		try {
			this.webServer = new WebServer(Resource.newResource(sampleSites));
		} catch (IOException e) {
			throw new CrawljaxException("Could not load resource", e);
		}
	}

	/**
	 * @param webfolder
	 *            The folder the web site is stored in.
	 * @param siteExtension
	 *            The extention of the site. Leave blank or <code>null</code> for no extention.
	 */
	protected SampleCrawler(Resource webfolder, String siteExtension) {
		this.siteExtension = Strings.nullToEmpty(siteExtension);
		this.webServer = new WebServer(webfolder);
	}

	/**
	 * @param webfolder
	 *            The folder the web site is stored in.
	 */
	protected SampleCrawler(Resource webfolder) {
		this(webfolder, "");
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
		crawlSpec = newCrawlSpecification();
		config = newCrawlConfiguartion();
		config.setCrawlSpecification(crawlSpec);
		hasSetup.set(true);
	}

	/**
	 * Override this method to specify a different configuration.
	 * 
	 * @return a new {@link CrawljaxConfiguration} to crawl with.
	 */
	protected CrawljaxConfiguration newCrawlConfiguartion() {
		return new CrawljaxConfiguration();
	}

	/**
	 * Override this method if you want to customize the {@link CrawlSpecification}. The default is
	 * setup with infinite crawl depth and {@link CrawlSpecification#clickDefaultElements()}.
	 * <p>
	 * You can get the {@link URL} you need for the Crawlspec constructor using {@link #getUrl()}.
	 * 
	 * @return The {@link CrawlSpecification} the crawl should run with.
	 */
	protected CrawlSpecification newCrawlSpecification() {
		CrawlSpecification crawlSpecification = new CrawlSpecification(getUrl());
		crawlSpecification.setDepth(0);
		crawlSpecification.clickDefaultElements();
		return crawlSpecification;
	}

	protected final String getUrl() {
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
