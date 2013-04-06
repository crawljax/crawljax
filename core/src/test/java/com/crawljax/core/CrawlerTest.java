package com.crawljax.core;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.core.plugin.Plugins;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.StateFlowGraph;
import com.crawljax.core.state.StateVertex;
import com.crawljax.test.BrowserTest;
import com.crawljax.test.RunWithWebServer;

/**
 * Test class for the Crawler testing.
 */
@Category(BrowserTest.class)
public class CrawlerTest {

	@ClassRule
	public static final RunWithWebServer SERVER = new RunWithWebServer("/site");

	private Collection<List<Eventable>> paths;
	private StateVertex index;

	private CrawljaxConfiguration buildController() throws ConfigurationException {
		CrawljaxConfigurationBuilder builder =
		        CrawljaxConfiguration.builderFor(SERVER.getSiteUrl() + "crawler/index.html");
		builder.crawlRules().click("a");
		return builder.build();
	}

	@Before
	public void setupController() throws ConfigurationException, CrawljaxException {
		CrawljaxController controller = new CrawljaxController(buildController());
		controller.run();
		paths = controller.getSession().getCrawlPaths();
		index = controller.getSession().getInitialState();
	}

	@Test
	public void testCrawler() throws ConfigurationException {
		final TestController controller = new TestController(buildController(), index);

		for (final List<Eventable> path : paths) {
			new Crawler(controller, path, "Follow Path", Plugins.noPlugins()) {
				@Override
				public void run() {
					try {
						super.init();
						List<Eventable> newPath = controller.getSession().getCurrentCrawlPath();
						assertThat(
						        "Path found by Controller driven Crawling equals the path found in the Crawler",
						        path, is(newPath));
						super.shutdown();
					} catch (InterruptedException e) {
						throw new AssertionError(e);
					}
				}
			}.run();
		}
		controller.getBrowserPool().shutdown();
	}

	private static class TestController extends CrawljaxController {
		CrawlSession localSession;
		StateFlowGraph g;
		StateVertex i;

		public TestController(CrawljaxConfiguration config, StateVertex index)
		        throws ConfigurationException {
			super(config);
			i = index;
			g = new StateFlowGraph(i);
			localSession =
			        new CrawlSession(this.getBrowserPool(), g, i, System.currentTimeMillis());
		}

		@Override
		public CrawlSession getSession() {
			return localSession;
		}
	}
}
