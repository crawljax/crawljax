package com.crawljax.plugins.clickabledetector;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.Every.everyItem;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.util.resource.Resource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.CrawlSession;
import com.crawljax.core.CrawljaxRunner;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.core.plugin.Plugins;
import com.crawljax.test.WebServer;
import com.google.common.base.Strings;

/**
 * This is both a test and an example of how the clickable detector works.
 */
@RunWith(MockitoJUnitRunner.class)
public class SiteRunnerCDWOProxy {

	private static final Logger LOG = LoggerFactory.getLogger(ClickableDetectorTest.class);
	private WebServer webServer = null;
	private String siteExtension;
	private static final long WAIT_TIME_AFTER_EVENT = 200;
	private static final long WAIT_TIME_AFTER_RELOAD = 20;

	@Mock
	Plugins plugins;

	private DomInterceptorPlugin interceptor;

	@Before
	public void before() {
		interceptor = new DomInterceptorPlugin();
		startServer(Resource.newClassPathResource("sites"), "homepage-with-jslibs");
	}

	public void startServer(Resource webfolder, String siteExtension) {
		this.siteExtension = Strings.nullToEmpty(siteExtension);
		LOG.debug("Loading web server with from folder {}", webfolder.getURL().toExternalForm());
		this.webServer = new WebServer(webfolder);
		try {
			webServer.start();
		} catch (Exception e) {
			throw new RuntimeException("Could not start the server", e);
		}
	}

	@After
	public void after() {
		webServer.stop();
	}

	@Test
	public void clickEverything() throws MalformedURLException {

		CrawljaxConfigurationBuilder conf = builderForClickDetector();

		CrawljaxRunner runner = new CrawljaxRunner(conf.build());
		CrawlSession session = runner.call();
		System.out.println("\n\n clickEverything States: "
		        + session.getStateFlowGraph().getAllStates().size() + " Edges: "
		        + session.getStateFlowGraph().getAllEdges().size());
		assertThat(interceptor.getIntercepted(), not(empty()));
		assertThat(interceptor.getIntercepted(), everyItem(containsString("data-cj-clickable")));

	}

	private CrawljaxConfigurationBuilder builderForClickDetector() {

		CrawljaxConfigurationBuilder conf = crawlDemoSetup();

		// Set timeouts
		conf.crawlRules().waitAfterReloadUrl(WAIT_TIME_AFTER_RELOAD, TimeUnit.MILLISECONDS);
		conf.crawlRules().waitAfterEvent(WAIT_TIME_AFTER_EVENT, TimeUnit.MILLISECONDS);

		ClickableDetectorFilter.configureCrawlRules(conf.crawlRules());
		return conf;
	}

	private CrawljaxConfigurationBuilder crawlDemoSetup() {
		URI uri = webServer.getSiteUrl().resolve(siteExtension);

		return CrawljaxConfiguration.builderFor(uri).addPlugin(interceptor)
		        .setBrowserConfig(new BrowserConfiguration(EmbeddedBrowser.BrowserType.CHROME));
	}

}
