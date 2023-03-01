package com.crawljax.test;

import com.crawljax.browser.BrowserProvider;
import com.crawljax.core.CrawlSession;
import com.crawljax.core.CrawljaxException;
import com.crawljax.core.CrawljaxRunner;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.core.plugin.Plugin;
import com.crawljax.core.state.PostCrawlStateGraphChecker;
import com.google.common.base.Strings;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;
import org.eclipse.jetty.util.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for Crawler examples.
 */
public class BaseCrawler {

    private static final Logger LOG = LoggerFactory.getLogger(BaseCrawler.class);
    private final WebServer webServer;
    private final AtomicBoolean hasSetup = new AtomicBoolean(false);

    private CrawljaxConfigurationBuilder configBuilder;
    private String siteExtension;

    /**
     * @param siteExtension Assumes the sites are in <code>resources/sites</code>.
     */
    public BaseCrawler(String siteExtension) {
        this.siteExtension = siteExtension;
        URL sampleSites = BaseCrawler.class.getResource("/site");
        LOG.debug("Loading web server with from folder {}", sampleSites.toExternalForm());

        this.webServer = new WebServer(Resource.newResource(sampleSites));
    }

    /**
     * @param webFolder     The folder the web site is stored in.
     * @param siteExtension The extension of the site. Leave blank or <code>null</code> for no
     *                      extension.
     */
    public BaseCrawler(Resource webFolder, String siteExtension) {
        this.siteExtension = Strings.nullToEmpty(siteExtension);
        try {
            LOG.debug(
                    "Loading web server with from folder {}",
                    webFolder.getURI().toURL().toExternalForm());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        this.webServer = new WebServer(webFolder);
    }

    /**
     * @param webFolder The folder the web site is stored in.
     */
    public BaseCrawler(Resource webFolder) {
        this(webFolder, "");
    }

    public WebServer getWebServer() {
        return webServer;
    }

    /**
     * Starts the web server, configures Crawljax. Doesn't run the Crawler. For that you need to call
     * {@link #crawl()}. After this method completes, you can configure the
     * {@link CrawljaxConfiguration} and the {@link CrawljaxConfiguration} using
     * {@link #newCrawlConfigurationBuilder()};
     * <p>
     * The {@link CrawljaxConfiguration} is configured with
     * {@link com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder} .
     * </p>
     */
    public void setup() {
        try {
            webServer.start();
        } catch (Exception e) {
            throw new RuntimeException("Could not start the server", e);
        }
        configBuilder = newCrawlConfigurationBuilder();
        hasSetup.set(true);
    }

    /**
     * Override this method to specify a different configuration.
     *
     * @return a new {@link CrawljaxConfiguration} to crawl with.
     */
    protected CrawljaxConfigurationBuilder newCrawlConfigurationBuilder() {
        CrawljaxConfigurationBuilder builder = CrawljaxConfiguration.builderFor(getUrl());
        builder.setBrowserConfig(new BrowserConfiguration(BrowserProvider.getBrowserType()));
        builder.crawlRules().clickDefaultElements();
        builder.setUnlimitedRuntime();
        builder.setUnlimitedCrawlDepth();
        builder.addPlugin(new PostCrawlStateGraphChecker());
        return builder;
    }

    protected URI getUrl() {
        return webServer.getSiteUrl().resolve(siteExtension);
    }

    /**
     * @return the {@link CrawljaxConfiguration} or <code>null</code> if {@link #setup()} or
     * {@link #crawl()} haven't been called yet.
     */
    public CrawljaxConfiguration getConfig() {
        return configBuilder.build();
    }

    /**
     * Runs the crawler. If you haven't run {@link #setup()} it will do that before it runs.
     *
     * @return {@link CrawlSession} for post-crawl inspection.
     * @throws CrawljaxException When crawljax isn't configured correctly or the web server fails to
     *                           stop.
     */
    public CrawlSession crawl() throws CrawljaxException {
        if (!hasSetup.get()) {
            setup();
        }
        CrawljaxRunner crawljax = new CrawljaxRunner(configBuilder.build());
        CrawlSession session = crawljax.call();
        webServer.stop();
        return session;
    }

    /**
     * Runs a crawl with the given plugins.
     *
     * @param plugins The plugins you want to run with the {@link CrawljaxConfiguration}.
     * @return The resulting {@link CrawlSession}.
     * @see #crawl()
     */
    public CrawlSession crawlWith(Plugin... plugins) {
        if (!hasSetup.get()) {
            setup();
        }
        for (Plugin plugin : plugins) {
            configBuilder.addPlugin(plugin);
        }
        return crawl();
    }

    /**
     * This starts the web server and blocks the thread so you can inspect the site in your browser
     * manually. The URL to visit is printed to the console.
     * <p>
     * This thread will block once started so you have to stop it manually, once you are done.
     *
     * @throws Exception in case the web server can't start
     */
    public void showWebSite() throws Exception {
        webServer.start();
        System.out.println(getUrl());
        webServer.join();
    }
}
