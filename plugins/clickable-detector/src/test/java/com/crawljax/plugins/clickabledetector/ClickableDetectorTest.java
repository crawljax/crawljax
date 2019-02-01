package com.crawljax.plugins.clickabledetector;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.Every.everyItem;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

import java.net.MalformedURLException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.CrawlSession;
import com.crawljax.core.CrawljaxRunner;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.core.plugin.Plugins;
import com.crawljax.core.state.StateFlowGraph;

/**
 * This is both a test and an example of how the clickable detector works.
 */
@RunWith(MockitoJUnitRunner.class)
public class ClickableDetectorTest {

	@Mock
	Plugins plugins;

	private DomInterceptorPlugin interceptor;

	@Before
	public void before() {
		interceptor = new DomInterceptorPlugin();
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
		CrawljaxProxyPlugin proxyPlugin = new CrawljaxProxyPlugin(55555);
		proxyPlugin.addFilter(ClickableDetectorFilter.withCss());

		CrawljaxConfigurationBuilder conf = crawlDemoSetup();

		conf.setProxyConfig(proxyPlugin.getConfiguration()).addPlugin(proxyPlugin);

		ClickableDetectorFilter.configureCrawlRules(conf.crawlRules());
		return conf;
	}

	private CrawljaxConfigurationBuilder crawlDemoSetup() {
		return CrawljaxConfiguration.builderFor("http://localhost:8080/").addPlugin(interceptor)
		        .setBrowserConfig(new BrowserConfiguration(EmbeddedBrowser.BrowserType.FIREFOX));
	}

	@Test
	public void clickableDetectorShouldFindSameStatesAsWithout()
	        throws MalformedURLException, ExecutionException, InterruptedException {
		ExecutorService executorService = Executors.newFixedThreadPool(2);
		Future<CrawlSession> withoutClickDetect =
		        executorService.submit(new CrawljaxRunner(crawlDemoSetup().build()));
		Future<CrawlSession> withClickDetect =
		        executorService.submit(new CrawljaxRunner(builderForClickDetector().build()));

		StateFlowGraph withClickDetectSfg = withClickDetect.get().getStateFlowGraph();
		StateFlowGraph withOutClickDetectSfg = withoutClickDetect.get().getStateFlowGraph();

		assertThat("Edges", withClickDetectSfg.getAllEdges().size(),
		        greaterThanOrEqualTo(withOutClickDetectSfg.getAllEdges().size()));

		assertThat("States", withClickDetectSfg.getAllStates().size(),
		        greaterThanOrEqualTo(withOutClickDetectSfg.getAllStates().size()));

		executorService.shutdownNow();
		executorService.awaitTermination(10, TimeUnit.SECONDS);
	}

}
